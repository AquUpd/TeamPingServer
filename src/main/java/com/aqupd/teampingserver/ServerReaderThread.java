package com.aqupd.teampingserver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.*;

@SuppressWarnings("ConstantConditions")
public class ServerReaderThread extends Thread {
  private final Socket socket;
  public static int step = 0;
  public static boolean init = true;
  public static boolean waitfordata = false;
  public ServerReaderThread(Socket socket) {
    this.socket = socket;
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
            waitfordata = false;
            step++;
          } else if (text.equals("YES") && step == 6) {
            System.out.println(step);
            System.out.println("Waiting for new data");
            init = false;
          }
        } else {
          System.out.println(text);
        }
      } while (!text.equals("DISCONNECT"));

    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}