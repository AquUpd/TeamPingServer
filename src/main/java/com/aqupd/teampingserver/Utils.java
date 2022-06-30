package com.aqupd.teampingserver;

import com.google.gson.JsonParser;

public class Utils {
  public static boolean isValidJsonObject(String input) {
    try {
      JsonParser.parseString(input).getAsJsonObject();
      return true;
    } catch (IllegalStateException ex) {
      return false;
    }
  }
}
