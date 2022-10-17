package com.aqupd.teampingserver.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Party {

  private transient final Logger LOG = LogManager.getLogger("TeamPing");

  private String partyName;
  private String owner;
  private List<Member> memberList = new ArrayList<>(8);
  private transient List<String> bannedPlayers = new ArrayList<>();

  public Party(String name, Member member) {
    this.owner = member.getNickname();
    this.memberList.add(member);
    this.partyName = name;
  }

  public boolean join(Member member) {
    if (bannedPlayers.contains(member.getNickname())) return false;
    if (memberList.contains(member)) return false;

    this.memberList.add(member);
    return true;
  }

  public boolean ban(String requestedUser, String bannedUser) {
    if(dissalowedToUse(requestedUser, bannedUser)) return false;
    bannedPlayers.add(bannedUser);
    kick(bannedUser, true, false);
    return true;
  }

  public boolean kick(String requestedUser, String kickedUser) {
    if(dissalowedToUse(requestedUser, kickedUser)) return false;
    kick(kickedUser, false, false);
    return true;
  }

  public void leave(String requestedUser) {
    kick(requestedUser, false, true);
    if(owner.equalsIgnoreCase(requestedUser)) owner = memberList.get(0).getNickname();
  }

  private boolean dissalowedToUse(String user, String user1) {
    if(!user.equalsIgnoreCase(owner)) return true;
    return user.equalsIgnoreCase(user1);
  }

  private void kick(String kickedUser, Boolean isBan, Boolean isDisconnect) {
    Member kicked;
    Iterator<Member> iter = memberList.iterator();
    while(iter.hasNext()) {
      Member iteratable = iter.next();
      if(iteratable.getNickname().equals(kickedUser)) { kicked = iteratable; iter.remove(); }
    }

    //Send "kicked" member message that he got kicked/banned

  }
}

