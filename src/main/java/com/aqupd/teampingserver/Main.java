package com.aqupd.teampingserver;

import java.io.*;
import java.net.*;

import com.aqupd.teampingserver.utils.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("InfiniteLoopStatement")
public class Main {

  public static final Logger LOGGER = LogManager.getLogger("TeamPing");

  public static void main(String[] args) {
    Config conf = Config.get;
    conf.load();

    try (ServerSocket serverSocket = new ServerSocket(conf.getPort(), 10, InetAddress.getByName(conf.getIp()))) {
      LOGGER.info("Server is listening on port " + serverSocket.getLocalPort());
      while (true) {
        Socket socket = serverSocket.accept();
        LOGGER.info("New client connected!");
      }
    } catch (IOException ex) {
      LOGGER.error("Server exception: ", ex);
    }
  }
}