package com.aqupd.teampingserver;

import static com.aqupd.teampingserver.Main.colors;
import static com.aqupd.teampingserver.Main.pingdata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Random;

public class ServerThreads {

  private final Socket socket;
  private int step = 0;
  private boolean init = true;
  private boolean waitfordata = false;
  private boolean closed = false;

  private Color randomcolor;
  private String nickname;

  public ServerThreads(Socket socket) {
    this.socket = socket;
    new Reader().start();
    new Writer().start();
    Random rng = new Random();
    randomcolor = colors.get(rng.nextInt(colors.size()));
  }

  private class Reader extends Thread{
    public void run() {
      System.out.println("reader " + currentThread().getName());
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
              System.out.println(step);
              step++;
            } else if (text.equals("DATA") && step == 2) {
              System.out.println(step);
              step++;
            } else if (waitfordata && step == 4) {
              System.out.println(step);
              data = JsonParser.parseString(text).getAsJsonObject();
              System.out.println(data);
              nickname = data.get("name").getAsString();
              waitfordata = false;
              step++;
            } else if (text.equals("YES") && step == 6) {
              System.out.println(step);
              System.out.println("Waiting for new data");
              init = false;
            }
          } else {
            data = JsonParser.parseString(text).getAsJsonObject();

            JsonArray clr = new JsonArray();
            clr.add(randomcolor.getRed());
            clr.add(randomcolor.getGreen());
            clr.add(randomcolor.getBlue());
            data.add("color", clr);
            data.add("nickname", new JsonPrimitive(nickname));
            pingdata = data;
          }
        } while (!text.equals("DISCONNECT"));
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
  private class Writer extends Thread{
    public void run() {
      System.out.println("writer " + currentThread().getName());
      try {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);

        do {
          if (socket.isClosed()) break;
          if (init) {
            if (step == 1) {
              System.out.println(step);
              writer.println("YES");
              step++;
            } else if (step == 3) {
              System.out.println(step);
              writer.println("YES");
              waitfordata = true;
              step++;
            } else if (step == 5) {
              System.out.println(step);
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
            if(pingdata.size() != 0 && !pingdata.get("nickname").getAsString().equals(nickname)){
              System.out.println(nickname + " " + pingdata);
              writer.println(pingdata);
              pingdata = new JsonObject();
            }
          }
        } while (!closed);
        System.out.println("Client disconnected! " + socket.getRemoteSocketAddress());
        interrupt();
      } catch (IOException ex) {
        interrupt();
      }
    }
  }
}
