package com.aqupd.teampingserver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.*;

@SuppressWarnings("ConstantConditions")
public class ServerThread extends Thread {
  private final Socket socket;

  public ServerThread(Socket socket) {this.socket = socket;}

  public void run() {
    try {
      InputStream input = socket.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(input));

      OutputStream output = socket.getOutputStream();
      PrintWriter writer = new PrintWriter(output, true);

      String text = "";
      int step = 0;
      boolean waitfordata = false;
      boolean init = true;
      JsonObject data;

      do {
        text = reader.readLine();
        if (init) {
          if (text.equals("CONNECT") && step == 0) {
            writer.println("YES");
            System.out.println(step + " done!");
            step++;
          } else if (text.equals("DATA") && step == 1) {
            writer.println("YES");
            waitfordata = true;
            System.out.println(step + " done!");
            step++;
          } else if (waitfordata && step == 2) {
            data = JsonParser.parseString(text).getAsJsonObject();
            waitfordata = false;
            //Some kind of check in the future
            System.out.println(step + " done! " + data);
            if (true) {
              writer.println("SUCCESS");
            } else {
              writer.println("NOTSUCCESS");
              break;
            }
            step++;
          } else if (text.equals("YES") && step == 3) {
            System.out.println(step + " done! Waiting for new data");
            init = false;
          }
        } else {
          System.out.println(text);
        }
      } while (!text.equals("DISCONNECT"));

      writer.println("DISCONNECT");
      System.out.println("Client disconnected!" + socket.getRemoteSocketAddress());
      socket.close();
    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}