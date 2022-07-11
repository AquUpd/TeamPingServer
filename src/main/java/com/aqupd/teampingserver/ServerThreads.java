package com.aqupd.teampingserver;

import static com.aqupd.teampingserver.Main.*;
import static com.aqupd.teampingserver.Utils.isValidJsonObject;

import com.google.gson.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
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
      String partyname = "";
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
                  playerCount++;
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

            switch (data.get("datatype").getAsString()) {
              case "ping":
                if (partyname.length() > 0) {
                  generatePingJson(partyname, data);
                }
                lastinteraction = System.currentTimeMillis();
                break;
              case "party":
                switch (data.get("subtype").getAsString()) {
                  case "connect":
                    String pname = data.get("partyname").getAsString();
                    if (partyname.length() == 0 && pname.length() >= 3 && pname.length() <= 32) {
                      LinkedHashMap<String, Socket> local = new LinkedHashMap<>();
                      if (parties.containsKey(pname)) {
                        if (banlist.get(pname).contains(nickname)) {
                          partyname = "";
                          JsonObject jo = new JsonObject();
                          jo.add("datatype", new JsonPrimitive("party"));
                          jo.add("subtype", new JsonPrimitive("kickmessage"));
                          jo.add("message", new JsonPrimitive("banned"));
                          sendDataToSocket(socket, jo);
                        } else if (parties.get(pname).size() > 8) {
                          partyname = "";
                          JsonObject jo = new JsonObject();
                          jo.add("datatype", new JsonPrimitive("party"));
                          jo.add("subtype", new JsonPrimitive("kickmessage"));
                          jo.add("message", new JsonPrimitive("playerlimit"));
                          sendDataToSocket(socket, jo);
                        } else {
                          parties.get(pname).put(nickname, socket);
                          partyname = pname;
                          sendPlayerList(partyname);
                        }
                      } else {
                        local.put(nickname, socket);
                        parties.put(pname, local);
                        banlist.put(pname, new ArrayList<>());
                        partyname = pname;
                        sendPlayerList(partyname);
                      }
                    }
                    break;
                  case "kick":
                    if (partyname.length() != 0 && parties.get(partyname).keySet().toArray()[0].equals(nickname)) {
                      String kickname = data.get("nick").getAsString();
                      if (!kickname.equals(nickname) && parties.get(partyname).containsKey(kickname)) {
                        Socket kickedsocket = parties.get(partyname).get(kickname);
                        parties.get(partyname).remove(kickname);
                        sendPlayerList(partyname);
                        JsonObject jo = new JsonObject();
                        jo.add("datatype", new JsonPrimitive("party"));
                        jo.add("subtype", new JsonPrimitive("kickmessage"));
                        jo.add("message", new JsonPrimitive("kicked"));
                        sendDataToSocket(kickedsocket, jo);
                      }
                    }
                    break;
                  case "ban":
                    if (partyname.length() != 0 && parties.get(partyname).keySet().toArray()[0].equals(nickname)) {
                      String banname = data.get("nick").getAsString();
                      if (!banname.equals(nickname) && parties.get(partyname).containsKey(banname)) {
                        Socket bannedsocket = parties.get(partyname).get(banname);
                        parties.get(partyname).remove(banname);
                        banlist.get(partyname).add(banname);
                        sendPlayerList(partyname);
                        JsonObject jo = new JsonObject();
                        jo.add("datatype", new JsonPrimitive("party"));
                        jo.add("subtype", new JsonPrimitive("kickmessage"));
                        jo.add("message", new JsonPrimitive("banned"));
                        sendDataToSocket(bannedsocket, jo);
                      }
                    }
                    break;
                  case "promote":
                    if (partyname.length() != 0 && parties.get(partyname).keySet().toArray()[0].equals(nickname)) {
                      String promotename = data.get("nick").getAsString();
                      LinkedHashMap<String, Socket> newPlayerList = new LinkedHashMap<>();
                      if (parties.get(partyname).containsKey(promotename)) {
                        newPlayerList.put(promotename, parties.get(partyname).get(promotename));
                        Map<String, Socket> conns = parties.get(partyname);
                        for (Map.Entry<String, Socket> client : conns.entrySet()) {
                          if (!Objects.equals(client.getKey(), promotename)) {
                            newPlayerList.put(client.getKey(), client.getValue());
                          }
                        }
                        parties.put(partyname, newPlayerList);
                        sendPlayerList(partyname);
                      }
                    }
                    break;
                  case "disconnect":
                    if (partyname.length() != 0) {
                      if (parties.get(partyname).get(nickname) != null) {
                        parties.get(partyname).remove(nickname);
                        if (parties.get(partyname).isEmpty()) {
                          parties.remove(partyname);
                          banlist.remove(partyname);
                        }
                        else sendPlayerList(partyname);
                      }
                      partyname = "";
                    }
                }
                LOGGER.info(parties.toString());
                break;
              case "list":
                JsonObject jo = new JsonObject();
                jo.add("datatype", new JsonPrimitive("list"));
                jo.add("connected", new JsonPrimitive(playerCount));
                writer.println(jo);
            }
          }
        } while (true);
        LOGGER.info("Connection stopped");
        socket.close();
        interrupt();
      } catch (IOException ex) {
        LOGGER.error("Connection exception");
      } finally {
        if(license && !init) {
          playerCount--;
          if(partyname.length() != 0) {
            parties.get(partyname).keySet().removeIf(nick -> nickname.equals(nick));
            if (parties.get(partyname).isEmpty()) {
              parties.remove(partyname);
              banlist.remove(partyname);
            }
            else sendPlayerList(partyname);
          }
        }
      }
    }
  }

  private void sendDataToSocket(Socket socket, JsonObject data) {
    try {
      PrintWriter swriter = new PrintWriter(socket.getOutputStream(), true);
      swriter.println(data);
    } catch (IOException ignored) {}
  }

  private void sendDataToParty(String party, JsonObject data) {
    Map<String, Socket> conns = parties.get(party);
    Iterator<Map.Entry<String, Socket>> clientMap = conns.entrySet().iterator();
    while (clientMap.hasNext()) {
      Map.Entry<String, Socket> client = clientMap.next();
      try {
        PrintWriter swriter = new PrintWriter(client.getValue().getOutputStream(), true);
        swriter.println(data);
      } catch (IOException ex) {
        if (client.getValue().isClosed()) clientMap.remove();
      }
    }
  }

  private void sendPlayerList(String party) {
    JsonObject jo = new JsonObject();
    JsonArray ja = new JsonArray();
    Map<String, Socket> conns1 = parties.get(party);
    for (String client : conns1.keySet()) ja.add(client);
    jo.add("datatype", new JsonPrimitive("party"));
    jo.add("subtype", new JsonPrimitive("list"));
    jo.add("players", ja);

    sendDataToParty(party, jo);
  }

  private void generatePingJson(String party, JsonObject jsonObject) {
    JsonObject ping = new JsonObject();
    JsonArray clr = new JsonArray();
    ping.add("datatype", new JsonPrimitive("ping"));

    JsonArray blockpos = new JsonArray();
    blockpos.add(jsonObject.get("bp").getAsJsonArray().get(0));
    blockpos.add(jsonObject.get("bp").getAsJsonArray().get(1));
    blockpos.add(jsonObject.get("bp").getAsJsonArray().get(2));
    ping.add("bp", blockpos);
    ping.add("type", jsonObject.get("type"));
    ping.add("isEntity", jsonObject.get("isEntity"));

    clr.add(randomcolor.getRed());
    clr.add(randomcolor.getGreen());
    clr.add(randomcolor.getBlue());

    ping.add("color", clr);
    ping.add("nickname", new JsonPrimitive(nickname));
    ping.add("time", new JsonPrimitive(System.currentTimeMillis()));
    ping.add("uuid", jsonObject.get("uuid"));

    sendDataToParty(party, ping);
  }
}
