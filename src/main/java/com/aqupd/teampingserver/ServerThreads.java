package com.aqupd.teampingserver;

import static com.aqupd.teampingserver.Main.LOGGER;
import static com.aqupd.teampingserver.Main.colors;
import static com.aqupd.teampingserver.Pings.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("FieldMayBeFinal")
public class ServerThreads {

  private final Socket socket;
  private Color randomcolor;
  private int step = 0;
  private long lastinteraction = 0;
  private boolean init = true;
  private boolean waitfordata = false;
  private boolean closed = false;
  private boolean license = false;
  private HashMap<Long, String> sentPings = new HashMap<>();
  private String nickname;
  private boolean debug;
  private Random rng = new Random();
  public ServerThreads(Socket socket) {
    this.socket = socket;
    ThreadGroup tg = new ThreadGroup(socket.getRemoteSocketAddress().toString());
    new Reader(tg, socket.getRemoteSocketAddress().toString() + " reader").start();
    new Writer(tg, socket.getRemoteSocketAddress().toString() + " writer").start();

    randomcolor = colors.get(rng.nextInt(colors.size()));
    this.debug = socket.getInetAddress().isLoopbackAddress();
  }

  private class Reader extends Thread {
    Reader(ThreadGroup tg, String name) {
      super(tg, name);
    }

    @Override
    public void run() {
      try {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String text;
        JsonObject data;

        socket.setSoTimeout(10000);
        do {
          text = reader.readLine();
          if (text == null) break;
          if (closed || socket.isClosed()) break;
          if (init) {
            if (text.equals("CONNECT") && step == 0) {
              LOGGER.info(step);
              step++;
              lastinteraction = System.currentTimeMillis();
            } else if (text.equals("DATA") && step == 2) {
              LOGGER.info(step);
              step++;
              lastinteraction = System.currentTimeMillis();
            } else if (waitfordata && step == 4 && text.length() != 0) {
              LOGGER.info(step + " " + text);
              data = JsonParser.parseString(text).getAsJsonObject();
              nickname = data.get("name").getAsString();
              String serverid = data.get("serverid").getAsString();
              if (!debug) {
                HttpsURLConnection con = (HttpsURLConnection) new URL(String.format("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s", nickname, serverid)).openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.setReadTimeout(250);

                try {
                  InputStream in = con.getInputStream();
                  BufferedReader readin = new BufferedReader(new InputStreamReader(in));
                  if (readin.readLine() != null) {
                    license = true;
                  }
                } catch (SocketTimeoutException ex) {
                  license = false;
                }
              } else {
                license = true;
              }
              waitfordata = false;
              step++;
              lastinteraction = System.currentTimeMillis();
            } else if (text.equals("YES") && step == 6) {
              LOGGER.info(step);
              LOGGER.info("Waiting for new data");
              init = false;
            }
          } else if (text.equals("PING")) {
          } else if ((System.currentTimeMillis() - lastinteraction) > 800) {
            data = JsonParser.parseString(text).getAsJsonObject();
            LOGGER.info("received ping" + data);

            JsonArray clr = new JsonArray();
            clr.add(randomcolor.getRed());
            clr.add(randomcolor.getGreen());
            clr.add(randomcolor.getBlue());
            data.add("color", clr);
            data.add("nickname", new JsonPrimitive(nickname));
            data.add("time", new JsonPrimitive(System.currentTimeMillis()));
            addPings(data);
            lastinteraction = System.currentTimeMillis();
          }
        } while (socket.isConnected());
        LOGGER.info("Reader stopped");
        closed = true;
        socket.close();
        interrupt();
      } catch(IOException ex) {
        LOGGER.error("Reader stopped");
        closed = true;
        interrupt();
      }
    }
  }

  private class Writer extends Thread {
    Writer(ThreadGroup tg, String name) {
      super(tg, name);
    }

    @Override
    public void run() {
      try {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);

        do {
          if (socket.isClosed()) break;
          if (init) {
            if (step == 1 && (System.currentTimeMillis() - lastinteraction) > 250) {
              LOGGER.info(step);
              writer.println("YES");
              step++;
            } else if (step == 3 && (System.currentTimeMillis() - lastinteraction) > 250) {
              LOGGER.info(step);
              writer.println("YES");
              waitfordata = true;
              step++;
            } else if (step == 5 && (System.currentTimeMillis() - lastinteraction) > 250) {
              LOGGER.info(step);

              if (license) {
                writer.println("SUCCESS");
              } else {
                writer.println("NOTSUCCESS");
                break;
              }
              step++;
            }
          } else {
            if (getPing().size() != 0) {
              JsonObject currentPing = getPing();
              if (!sentPings.containsValue(currentPing.get("uuid").getAsString())) {
                sentPings.put(System.currentTimeMillis(), currentPing.get("uuid").getAsString());
                writer.println(currentPing);
              }
            }
            sentPings.keySet().removeIf(data -> (System.currentTimeMillis() - data) > 100);
          }
        } while (!closed);
        closed = true;
        LOGGER.info("Client disconnected! " + socket.getRemoteSocketAddress());
        interrupt();
      } catch (IOException ex) {
        LOGGER.error("Writer exception");
        interrupt();
      }
    }
  }
}
