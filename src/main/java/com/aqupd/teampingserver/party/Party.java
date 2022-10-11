package com.aqupd.teampingserver.party;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Party {

  String partyName;
  Member owner;
  List<Member> memberList = new ArrayList<>();
  List<String> bannedPlayers = new ArrayList<>();

  public Party(String name, Member member) {
    this.owner = member;
    this.memberList.add(member);
    this.partyName = name;
  }

  public boolean join(Member member) {
    if (bannedPlayers.contains(member.getNickname())) return false;
    if (memberList.contains(member)) return false;

    this.memberList.add(member);
    return true;
  }

  @Override
  public String toString() {
    return "{\"name\":\"" + partyName + "\"," +
        "\"members\":[" + memberList.stream().map(Member::toString).collect(Collectors.joining(",")) + "]," +
        "\"owner\":" + owner.toString() + "}";
  }
}

