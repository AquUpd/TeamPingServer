package com.aqupd.teampingserver.utils;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class Config {

  private Config() {}                       //Create Instance
  public static Config get = new Config();  //Of this class

  private final Logger LOG = LogManager.getLogger("TeamPing");
  private final File confFile = new File("./config.json");
  Gson gson = new GsonBuilder().setPrettyPrinting().create();

  //All the data
  private boolean debug = false;
  private String ip = "0.0.0.0";
  private int port = 28754;

  //Getters
  public String getIp() { return ip; }
  public int getPort() { return port; }
  public boolean isDebug() { return debug; }

  //Config loader
  public void load() {
    if (!confFile.exists() || confFile.length() == 0) save();
    try {
      JsonObject data = gson.fromJson(new FileReader(confFile), JsonObject.class);
      JsonElement setting;
      if((setting = data.get("debug")) != null) debug = setting.getAsBoolean();
      if((setting = data.get("ip")) != null) ip = setting.getAsString();
      if((setting = data.get("port")) != null) port = setting.getAsInt();
    } catch (FileNotFoundException ex) {
      LOG.trace("Couldn't load configuration file", ex);
    }
  }

  //Config saver
  public void save() {
    try {
      if (!confFile.exists()) { confFile.getParentFile().mkdirs(); confFile.createNewFile(); }

      JsonObject jo = new JsonObject();
      jo.add("debug", new JsonPrimitive(debug));
      jo.add("ip", new JsonPrimitive(ip));
      jo.add("port", new JsonPrimitive(port));

      PrintWriter printwriter = new PrintWriter(new FileWriter(confFile));
      printwriter.print(gson.toJson(jo));
      printwriter.close();
    } catch (IOException ex) {
      LOG.trace("Couldn't save configuration file", ex);
    }
  }
}
