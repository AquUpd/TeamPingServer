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

      String text;
      int step = 0;
      long lasttime = System.currentTimeMillis();
      boolean disconTimer = true;
      boolean waitfordata = false;
      JsonObject data;

      do {
        text = reader.readLine();
        if (disconTimer) {
          if ((System.currentTimeMillis() - lasttime) > 5000) break;
          if (text.equals("CONNECT") && step == 0) {
            lasttime = System.currentTimeMillis();
            writer.println("YES");
            System.out.println(step + " done!");
            step++;
          } else if (text.equals("DATA") && step == 1) {
            lasttime = System.currentTimeMillis();
            writer.println("YES");
            waitfordata = true;
            System.out.println(step + " done!");
            step++;
          } else if (text.length() != 0 && waitfordata && step == 2) {
            lasttime = System.currentTimeMillis();
            data = JsonParser.parseReader(reader).getAsJsonObject();
            waitfordata = false;
            //Some kind of check in the future
            System.out.println(step + " done! " + data);
            if(true){
              writer.println("SUCCESS");
            } else {
              writer.println("NOTSUCCESS");
              break;
            }
            step++;
          } else if (text.equals("YES") && step == 3) {
            disconTimer = false;
            System.out.println(step + " done! Waiting for new data");
          }
        }
      } while (true);

      writer.println("DISCONNECT");
      System.out.println("Client disconnected!" + socket.getLocalAddress() + ":" + socket.getLocalPort());
      socket.close();
    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}