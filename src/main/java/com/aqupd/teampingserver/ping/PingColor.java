package com.aqupd.teampingserver.ping;


public class PingColor {

  private long dec;

  public PingColor(int red, int green, int blue) {
    red = Math.min(red, 255); red = Math.max(red, 0);
    green = Math.min(green, 255); green = Math.max(green, 0);
    blue = Math.min(blue, 255); blue = Math.max(blue, 0);

    dec = red << 16 | green << 8 | blue;
  }

  public PingColor(long colorDec) {
    colorDec = Math.min(colorDec, 16777215); colorDec = Math.max(colorDec, 0);
    dec = colorDec;
  }

}
