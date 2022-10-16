package com.aqupd.teampingserver.ping;

import com.aqupd.teampingserver.party.Member;

public class Ping {

  private Position pos;
  private Member member;
  private boolean isEntity;

  public Ping(Position pos, Member member, Boolean isEntity) {
    this.pos = pos;
    this.member = member;
    this.isEntity = isEntity;
  }

}
