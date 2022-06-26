package com.aqupd.teampingserver;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("InfiniteLoopStatement")
public class Main {
  public static Map<Integer, Color> colors = new HashMap<>();
  public static final Logger LOGGER = LogManager.getLogger("TeamPing");

  public static void main(String[] args) {
    colors.put(0, Color.decode("#f44336"));
    colors.put(1, Color.decode("#e81e63"));
    colors.put(2, Color.decode("#9c27b0"));
    colors.put(3, Color.decode("#673ab7"));
    colors.put(4, Color.decode("#3f51b5"));
    colors.put(5, Color.decode("#2196f3"));
    colors.put(6, Color.decode("#03a9f4"));
    colors.put(7, Color.decode("#00bcd4"));
    colors.put(8, Color.decode("#009688"));
    colors.put(9, Color.decode("#4caf50"));
    colors.put(10, Color.decode("#8bc34a"));
    colors.put(11, Color.decode("#cddc39"));
    colors.put(12, Color.decode("#ffeb3b"));
    colors.put(13, Color.decode("#ffc107"));
    colors.put(14, Color.decode("#ff9800"));
    colors.put(15, Color.decode("#ff5722"));

    new Pings.PingsCleaner().start();
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