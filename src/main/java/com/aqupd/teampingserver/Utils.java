package com.aqupd.teampingserver;

import com.google.gson.Gson;

public class Utils {
  private static final Gson gson = new Gson();

  public static boolean isJSONValid(String jsonInString) {
    try {
      gson.fromJson(jsonInString, Object.class);
      return true;
    } catch(com.google.gson.JsonSyntaxException ex) {
      return false;
    }
  }
}
