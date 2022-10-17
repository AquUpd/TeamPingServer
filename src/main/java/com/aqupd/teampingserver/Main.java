package com.aqupd.teampingserver;

import com.aqupd.teampingserver.utils.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

  private final Logger LOG = LogManager.getLogger("TeamPing");
  public static final Gson GSON = new GsonBuilder().create();

  public static void main(String[] args) {
    Config conf = Config.get;
    conf.load();

    /*
    try (ServerSocket serverSocket = new ServerSocket(conf.getPort(), 10, InetAddress.getByName(conf.getIp()))) {
      LOGGER.info("Server is listening on port " + serverSocket.getLocalPort());
      while (true) {
        Socket socket = serverSocket.accept();
        LOGGER.info("New client connected!");
      }
    } catch (IOException ex) {
      LOGGER.error("Server exception: ", ex);
    }
    */
  }
}