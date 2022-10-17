package com.aqupd.teampingserver.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Member {

  private transient final Logger LOG = LogManager.getLogger("TeamPing");

  private String nickname;
  private PingColor color;
  private transient Socket socket;

  public Member(Socket socket, String name, PingColor color) {
    this.socket = socket;
    this.nickname = name;
    this.color = color;
  }

  public Socket getSocket() { return socket; }
  public String getNickname() { return nickname; }
  public PingColor getColor() { return color; }

  public void setColor(PingColor color) { this.color = color; }

  public void sendData(String data) {
    try {
      OutputStream output = socket.getOutputStream();
      PrintWriter writer = new PrintWriter(output, true);
      writer.println(data);
    } catch(IOException ex) {
      LOG.error("Got exception while trying to write to the socket", ex);
    }
  }
}
