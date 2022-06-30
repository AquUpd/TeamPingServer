package com.aqupd.teampingserver;

import static com.aqupd.teampingserver.Main.*;
import static com.aqupd.teampingserver.Utils.isValidJsonObject;

import com.google.gson.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;

public class ServerThreads {

  private final Color randomcolor;
  private final Socket socket;

  private final boolean debug;
  private String nickname;
  private String partyname = "";

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
          if (init) {
            if (System.currentTimeMillis() - lastinteraction > 250) {
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
                  } catch (SocketTimeoutException ignored) {
                  }
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
                LOGGER.info("Waiting for data");
              }
            }
          } else if (isValidJsonObject(text) && (System.currentTimeMillis() - lastinteraction) > 800) {
            data = JsonParser.parseString(text).getAsJsonObject();
            LOGGER.info("received data" + data);

            switch (data.get("datatype").getAsString()) {
              case "ping":
                JsonObject ping = new JsonObject();
                JsonArray clr = new JsonArray();
                ping.add("datatype", new JsonPrimitive("ping"));

                JsonArray blockpos = new JsonArray();
                blockpos.add(data.get("bp").getAsJsonArray().get(0));
                blockpos.add(data.get("bp").getAsJsonArray().get(1));
                blockpos.add(data.get("bp").getAsJsonArray().get(2));
                ping.add("bp", blockpos);
                ping.add("type", data.get("type"));
                ping.add("isEntity", data.get("isEntity"));

                clr.add(randomcolor.getRed());
                clr.add(randomcolor.getGreen());
                clr.add(randomcolor.getBlue());

                ping.add("color", clr);
                ping.add("nickname", new JsonPrimitive(nickname));
                ping.add("time", new JsonPrimitive(System.currentTimeMillis()));
                ping.add("uuid", data.get("uuid"));

                if(partyname.length() > 0){
                  Map<String, Socket> conns = parties.get(partyname);
                  Iterator<Map.Entry<String, Socket>> clientMap = conns.entrySet().iterator();
                  while (clientMap.hasNext()) {
                    Map.Entry<String, Socket> client = clientMap.next();
                    try {
                      PrintWriter swriter = new PrintWriter(client.getValue().getOutputStream(), true);
                      swriter.println(ping);
                    } catch (SocketException ex) {
                      if (client.getValue().isClosed()) clientMap.remove();
                    }
                  }
                }

                lastinteraction = System.currentTimeMillis();
                break;
              case "party":
                switch (data.get("subtype").getAsString()) {
                  case "connect":
                    partyname = data.get("partyname").getAsString();
                    Map<String, Socket> local = new HashMap<>();
                    if (parties.containsKey(partyname)) {
                      parties.get(partyname).put(nickname, socket);
                    } else {
                      local.put(nickname, socket);
                      parties.put(partyname, local);
                    }
                    JsonObject jo = new JsonObject();
                    JsonArray ja = new JsonArray();
                    Map<String, Socket> conns = parties.get(partyname);
                    for (String client : conns.keySet()) ja.add(client);
                    jo.add("datatype", new JsonPrimitive("party"));
                    jo.add("subtype", new JsonPrimitive("list"));
                    jo.add("players", ja);

                    writer.println(jo);
                    LOGGER.info(parties.get(partyname).toString() + " " + jo);
                    break;
                  case "kick":

                    break;
                  case "disconnect":
                    parties.get(partyname).remove(nickname);
                    if (parties.get(partyname).isEmpty()) parties.remove(partyname);
                    break;
                }
                break;
            }
          }
        } while (true);
        LOGGER.info("Connection stopped");
        socket.close();
        interrupt();
      } catch (IOException ex) {
        LOGGER.error("Connection exception");
      } finally {
        if(license && !init && partyname.length() != 0) {
          parties.get(partyname).keySet().removeIf(nick -> nickname.equals(nick));
          if (parties.get(partyname).isEmpty()) parties.remove(partyname);
          System.out.println(parties.toString());
        }
      }
    }
  }
}
