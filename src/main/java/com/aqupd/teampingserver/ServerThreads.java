package com.aqupd.teampingserver;

import static com.aqupd.teampingserver.Main.*;
import static com.aqupd.teampingserver.Pings.*;

import com.google.gson.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServerThreads {

  private final Socket socket;
  private final Color randomcolor;
  private int step = 0;
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

        do {
          if (socket.isClosed()) break;
          text = reader.readLine();
          if (text == null) break;
          if (init) {
            if (text.equals("CONNECT") && step == 0) {
              LOGGER.info(step);
              step++;
            } else if (text.equals("DATA") && step == 2) {
              LOGGER.info(step);
              step++;
            } else if (waitfordata && step == 4) {
              data = JsonParser.parseString(text).getAsJsonObject();
              LOGGER.info(step + " " + data);
              nickname = data.get("name").getAsString();
              waitfordata = false;
              step++;
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
            if (!removingPings) Pings.addPings(data);
          }
        } while (!text.equals("DISCONNECT"));
        LOGGER.info("Reader stopped");
        closed = true;
        socket.close();
        interrupt();
      } catch (IOException ex) {
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
            if (step == 1) {
              LOGGER.info(step);
              writer.println("YES");
              step++;
            } else if (step == 3) {
              LOGGER.info(step);
              writer.println("YES");
              waitfordata = true;
              step++;
            } else if (step == 5) {
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
            if(!removingPings){
              List<String> newSentPings = sentPings;
              JsonArray ja = Pings.getPings().deepCopy();
              for(JsonElement je : ja) {
                JsonObject jo = je.getAsJsonObject();
                if(!sentPings.contains(jo.get("uuid").getAsString()) && !jo.get("nickname").getAsString().equals(nickname)) {
                  newSentPings.add(jo.get("uuid").getAsString());
                  writer.println(jo);
                }
              }
              sentPings = newSentPings;
            }
          }
        } while (!closed);
        LOGGER.info("Client disconnected! " + socket.getRemoteSocketAddress());
        interrupt();
      } catch (IOException ex) {
        interrupt();
      }
    }
  }
}
