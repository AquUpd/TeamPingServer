package com.aqupd.teampingserver.party;

import java.awt.*;
import java.net.Socket;

public class Member {

  Socket socket;
  String nickname;
  Color color;

  public Member(Socket socket, String name, Color color) {
    this.socket = socket;
    this.nickname = name;
    this.color = color;
  }

  public Socket getSocket() { return socket; }
  public String getNickname() { return nickname; }
  public Color getColor() { return color; }

  public void setColor(Color color) { this.color = color; }

  @Override
  public String toString() {
    return "{\"nickname\":\"" + nickname + "\",\"color\":" + (256 * 256 * color.getRed() + 256 * color.getGreen() + color.getBlue()) + "}";
  }
}
