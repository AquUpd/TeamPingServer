package com.aqupd.teampingserver;

import java.awt.*;
import java.net.*;

import com.aqupd.teampingserver.party.Member;
import com.aqupd.teampingserver.party.Party;
import com.aqupd.teampingserver.utils.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("InfiniteLoopStatement")
public class Main {

  public static final Logger LOGGER = LogManager.getLogger("TeamPing");

  public static void main(String[] args) {
    Config conf = Config.get;
    conf.load();
    Member member1 = new Member(new Socket(), "member1", new Color(100, 10, 232));
    Member member2 = new Member(new Socket(), "member2", new Color(231, 230, 13));
    Member member3 = new Member(new Socket(), "member3", new Color(0, 120, 122));
    Party testParty = new Party("test", member1);
    testParty.join(member2);
    testParty.join(member3);

    LOGGER.info(testParty.toString());
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