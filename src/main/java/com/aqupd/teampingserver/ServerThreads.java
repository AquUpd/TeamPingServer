package com.aqupd.teampingserver;

import static com.aqupd.teampingserver.Main.*;

import com.google.gson.*;
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
      boolean writedata = false;
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
          if (!writedata) text = reader.readLine(); else text = "";
          if (text == null || socket.isClosed()) break;
          if (init && (System.currentTimeMillis() - lastinteraction) > 250) {
            if (step == 0 && text.length() != 0) {
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
              writedata = true;
              step++;
              lastinteraction = System.currentTimeMillis();
            } else if (step == 1) {
              LOGGER.info(step);
              if (license) {
                writer.println("SUCCESS");
              } else {
                writer.println("NOTSUCCESS");
                break;
              }
              writedata = false;
              init = false;
              conns.put(nickname, socket);
              LOGGER.info("Waiting for data");
            }
          } else if (Utils.isJSONValid(text) && (System.currentTimeMillis() - lastinteraction) > 800) {
            data = JsonParser.parseString(text).getAsJsonObject();
            LOGGER.info("received data" + data);

            String pingtype = data.get("datatype").getAsString();
            switch (pingtype){
              case "ping":
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
                break;
              case "party":

                break;
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
