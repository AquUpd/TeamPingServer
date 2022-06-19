package com.aqupd.teampingserver;

import java.io.*;
import java.net.*;

public class Main {
  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(13577)) {

      System.out.println("Server is listening on port " + 13577);

      while (true) {
        Socket socket = serverSocket.accept();
        System.out.println("New client connected! " + socket.getLocalAddress() + ":" + socket.getLocalPort());

        new ServerThread(socket).start();
      }

    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}