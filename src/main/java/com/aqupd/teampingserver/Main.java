package com.aqupd.teampingserver;

import com.google.gson.JsonObject;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("InfiniteLoopStatement")
public class Main {
  public static Map<Integer, Color> colors = new HashMap<>();
  public static JsonObject pingdata = new JsonObject();
  public static final Logger LOGGER = LogManager.getLogger("TeamPing");

  public static void main(String[] args) {
    colors.put(0, new Color(240,49,36));
    colors.put(1, new Color(249,155,31));
    colors.put(2, new Color(242,235,58));
    colors.put(3, new Color(97,177,70));
    colors.put(4, new Color(20,149,207));
    colors.put(5, new Color(62,94,171));
    colors.put(6, new Color(124,54,150));
    colors.put(7, new Color(167,30,72));
    try (ServerSocket serverSocket = new ServerSocket(28754)) {

      LOGGER.info("Server is listening on port " + serverSocket.getLocalPort());
      while (true) {
        Socket socket = serverSocket.accept();
        LOGGER.info("New client connected! " + socket.getRemoteSocketAddress());

        new ServerThreads(socket);
      }

    } catch (IOException ex) {
      LOGGER.error("Server exception: ", ex);
      ex.printStackTrace();
    }
  }
}