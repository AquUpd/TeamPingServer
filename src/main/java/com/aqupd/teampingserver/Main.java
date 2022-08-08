package com.aqupd.teampingserver;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("InfiniteLoopStatement")
public class Main {
  public static Map<String, LinkedHashMap<String, Socket>> parties = new HashMap<>();
  public static Map<String, List<String>> banlist = new HashMap<>();
  public static int playerCount = 0;
  public static final Logger LOGGER = LogManager.getLogger("TeamPing");
  public static final String version = "0.1.2";

  public static Color[] colors = new Color[] {
    Color.decode("#f44336"),
    Color.decode("#e81e63"),
    Color.decode("#9c27b0"),
    Color.decode("#673ab7"),
    Color.decode("#3f51b5"),
    Color.decode("#2196f3"),
    Color.decode("#03a9f4"),
    Color.decode("#00bcd4"),
    Color.decode("#009688"),
    Color.decode("#4caf50"),
    Color.decode("#8bc34a"),
    Color.decode("#cddc39"),
    Color.decode("#ffeb3b"),
    Color.decode("#ffc107"),
    Color.decode("#ff9800"),
    Color.decode("#ff5722")
  };

  public static void main(String[] args) {

    try {
      Path dir = Paths.get(System.getProperty("user.dir") + "/logs");
      Files.createDirectory(dir);
    } catch (IOException ignored) {}

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