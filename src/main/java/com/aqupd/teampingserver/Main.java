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
  public static String version = "debug";
  public static Long lastVersionCheck = 0L;
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
      URL url = new URL("https://raw.githubusercontent.com/Ivan-Khar/TeamPing/master/CurrentVersion.txt");
      Scanner s = new Scanner(url.openStream());
      version = s.next();
      lastVersionCheck = System.currentTimeMillis();
    } catch(IOException ex) {
      LOGGER.error("Couldn't get current version of the mod", ex);
    }

    try (ServerSocket serverSocket = new ServerSocket(28754)) {
      LOGGER.info("Server is listening on port " + serverSocket.getLocalPort());
      while (true) {
        Socket socket = serverSocket.accept();
        String threadname = socket.getRemoteSocketAddress().toString();
        LOGGER.info("New client connected! " + threadname.substring(1, threadname.indexOf(".")) + ".##" + threadname.substring(threadname.length()-9));
        new ServerThreads(socket);
      }
    } catch (IOException ex) {
      LOGGER.error("Server exception: ", ex);
    }
  }
}