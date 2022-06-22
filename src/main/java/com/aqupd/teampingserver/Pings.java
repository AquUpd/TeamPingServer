package com.aqupd.teampingserver;

import static com.aqupd.teampingserver.Main.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Pings {
  private static JsonArray pings = new JsonArray();
  private static long lastchecktime = System.currentTimeMillis();
  public static boolean removingPings = false;

  public static void addPings(JsonObject ping) {
    pings.add(ping);
    System.out.println(pings);
  }

  public static JsonArray getPings() {
    return pings;
  }

  @SuppressWarnings("InfiniteLoopStatement")
  public static class PingsCleaner extends Thread {
    PingsCleaner() { super(); }

    public void run() {
      while(true) {
        if((System.currentTimeMillis() - lastchecktime) > 1000){
          removingPings = true;
          JsonArray temppings = pings.deepCopy();
          JsonArray newPings = new JsonArray();
          for(JsonElement je : temppings) {
            JsonObject jo = je.getAsJsonObject();
            if (!((System.currentTimeMillis() - jo.get("time").getAsLong()) > 2000)) newPings.add(jo);
          }
          pings = newPings;
          lastchecktime = System.currentTimeMillis();
        }
        removingPings = false;
      }
    }
  }
}
