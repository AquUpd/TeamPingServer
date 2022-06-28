package com.aqupd.teampingserver;

import static com.aqupd.teampingserver.Main.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.net.ssl.HttpsURLConnection;

public class ServerThreads {

  private final Color randomcolor;
  private final Socket socket;

  private final boolean debug;
  private String nickname;

  public ServerThreads(Socket socket) {
    this.socket = socket;
    new Client(socket.getRemoteSocketAddress().toString()).start();

    Random rng = new Random();
    randomcolor = colors.get(rng.nextInt(colors.size()));
    this.debug = socket.getInetAddress().isLoopbackAddress();
  }

  private class Client extends Thread {
    Client(String name) { super(name); }

    @Override
    public void run() {
      boolean license = false;
      boolean init = true;
      boolean waitfordata = false;
      boolean initwrite = false;
      long lastinteraction = 0;
      int step = 0;

      try {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);

        String text;
        JsonObject data;

        socket.setSoTimeout(5000);
        do {
          if (!initwrite) text = reader.readLine(); else text = "";
          if (text == null || socket.isClosed()) break;
          if (init && (System.currentTimeMillis() - lastinteraction) > 250) {
            if (text.equals("CONNECT") && step == 0) {
              LOGGER.info(step);
              initwrite = true;
              step++;
              lastinteraction = System.currentTimeMillis();
            } else if (step == 1) {
              LOGGER.info(step);
              writer.println("YES");
              initwrite = false;
              step++;
            } else if (text.equals("DATA") && step == 2) {
              LOGGER.info(step);
              initwrite = true;
              waitfordata = true;
              step++;
              lastinteraction = System.currentTimeMillis();
            } else if (step == 3) {
              LOGGER.info(step);
              writer.println("YES");
              initwrite = false;
              step++;
            } else if (waitfordata && step == 4 && text.length() != 0) {
              LOGGER.info(step + " " + text);
              data = JsonParser.parseString(text).getAsJsonObject();
              nickname = data.get("name").getAsString();
              String serverid = data.get("serverid").getAsString();
              if (!debug) {
                HttpsURLConnection con = (HttpsURLConnection) new URL(String.format("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s", nickname, serverid)).openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.setReadTimeout(500);
                try {
                  InputStream in = con.getInputStream();
                  BufferedReader readin = new BufferedReader(new InputStreamReader(in));
                  if (readin.readLine() != null) license = true;
                } catch (SocketTimeoutException ignored) {}
              } else {
                license = true;
              }
              waitfordata = false;
              initwrite = true;
              step++;
              lastinteraction = System.currentTimeMillis();
            } else if (step == 5) {
              LOGGER.info(step);
              if (license) {
                writer.println("SUCCESS");
              } else {
                writer.println("NOTSUCCESS");
                break;
              }
              initwrite = false;
              step++;
            } else if (text.equals("YES") && step == 6) {
              LOGGER.info(step + " Waiting for new data");
              init = false;
              conns.put(nickname, socket);
            }
          } else if (!text.equals("PING") && (System.currentTimeMillis() - lastinteraction) > 800) {
            data = JsonParser.parseString(text).getAsJsonObject();
            LOGGER.info("received ping" + data);

            JsonArray clr = new JsonArray();
            clr.add(randomcolor.getRed());
            clr.add(randomcolor.getGreen());
            clr.add(randomcolor.getBlue());
            data.add("color", clr);
            data.add("nickname", new JsonPrimitive(nickname));
            data.add("time", new JsonPrimitive(System.currentTimeMillis()));

            Iterator<Map.Entry<String, Socket>> clientMap = conns.entrySet().iterator();
            while(clientMap.hasNext()){
              Map.Entry<String, Socket> client = clientMap.next();
              try {
                PrintWriter swriter = new PrintWriter(client.getValue().getOutputStream(), true);
                swriter.println(data);
              } catch (SocketException ex) {
                if (client.getValue().isClosed()) clientMap.remove();
              }
            }
            lastinteraction = System.currentTimeMillis();
          }
        } while (true);
        LOGGER.info("Connection stopped");
        if(license && !init) {
          conns.keySet().removeIf(nick -> nickname.equals(nick));
        }
        socket.close();
        interrupt();
      } catch (IOException ex) {
        LOGGER.error("Connection exception");
        if(license && !init) {
          conns.keySet().removeIf(nick -> nickname.equals(nick));
        }
      }
    }
  }
}
