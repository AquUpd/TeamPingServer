package com.aqupd.teampingserver;

import java.io.*;
import java.net.*;

@SuppressWarnings("InfiniteLoopStatement")
public class Main {
  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(28754)) {

      System.out.println("Server is listening on port " + serverSocket.getLocalPort());

      while (true) {
        Socket socket = serverSocket.accept();
        System.out.println("New client connected! " + socket.getRemoteSocketAddress());

        new ServerThread(socket).start();
      }

    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}