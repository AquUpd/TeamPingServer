package com.aqupd.teampingserver;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PingsData {
  private JsonArray pings = new JsonArray();

  public void addPings(JsonObject pings) {
    this.pings.add(pings);
  }

  public JsonArray getPings() {
    return pings;
  }
}
