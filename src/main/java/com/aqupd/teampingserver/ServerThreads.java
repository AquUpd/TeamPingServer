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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("FieldMayBeFinal")
public class ServerThreads {

  private final Socket socket;
  private Color randomcolor;
  private int step = 0;
  private long lastinteraction = 0;
  private boolean init = true;
  private boolean waitfordata = false;
  private boolean closed = false;
  private List<String> sentPings = new ArrayList<>();
  private String nickname;

  public ServerThreads(Socket socket) {
    this.socket = socket;
    ThreadGroup tg = new ThreadGroup(socket.getRemoteSocketAddress().toString());
    new Reader(tg, "Reader").start();
    new Writer(tg, "Writer").start();
    Random rng = new Random();
    randomcolor = colors.get(rng.nextInt(colors.size()));
  }

  private class Reader extends Thread {
    Reader(ThreadGroup tg, String name) {
      super(tg, name);
    }

    public void run() {
      try {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String text;
        JsonObject data;
        while ((text = reader.readLine()) != null) {
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
            } else if (waitfordata && step == 4) {
              data = JsonParser.parseString(text).getAsJsonObject();
              LOGGER.info(step + " " + data);
              nickname = data.get("name").getAsString();
              waitfordata = false;
              step++;
              lastinteraction = System.currentTimeMillis();
            } else if (text.equals("YES") && step == 6) {
              LOGGER.info(step);
              LOGGER.info("Waiting for new data");
              init = false;
            }
          } else {
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
          }
        }
        LOGGER.info("Reader stopped");
        closed = true;
        socket.close();
        interrupt();
      } catch(IOException ex){
        closed = true;
        interrupt();
      }
    }
  }

  @SuppressWarnings("ConstantConditions")
  private class Writer extends Thread {
    Writer(ThreadGroup tg, String name) {
      super(tg, name);
    }

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
              //Some kind of check in the future
              if (true) {
                writer.println("SUCCESS");
              } else {
                writer.println("NOTSUCCESS");
                break;
              }
              step++;
            }
          } else {
            if(getPing().size() != 0) {
              JsonObject currentPing = getPing();
              if(!sentPings.contains(currentPing.get("uuid").getAsString())) {
                sentPings.add(currentPing.get("uuid").getAsString());
                writer.println(currentPing);
              }
            }
          }
        } while (!closed);
        closed = true;
        LOGGER.info("Client disconnected! " + socket.getRemoteSocketAddress());
        interrupt();
      } catch (IOException ex) {
        interrupt();
      }
    }
  }
}
