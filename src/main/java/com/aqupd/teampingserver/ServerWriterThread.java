package com.aqupd.teampingserver;

import static com.aqupd.teampingserver.ServerReaderThread.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerWriterThread extends Thread {
  private final Socket socket;

  public ServerWriterThread(Socket socket) {
    this.socket = socket;
  }

  public void run() {
    try {
      OutputStream output = socket.getOutputStream();
      PrintWriter writer = new PrintWriter(output, true);

      do {
        if (socket.isClosed()) break;
        if (init) {
          if (step == 1){
            System.out.println(step);
            writer.println("YES");
            step++;
          } else if (step == 3) {
            System.out.println(step);
            writer.println("YES");
            waitfordata = true;
            step++;
          } else if (step == 5) {
            System.out.println(step);
            //Some kind of check in the future
            if (true) {
              writer.println("SUCCESS");
            } else {
              writer.println("NOTSUCCESS");
              break;
            }
            step++;
          }
        }
      } while(true);

      writer.println("DISCONNECT");
      System.out.println("Client disconnected!" + socket.getRemoteSocketAddress());
      socket.close();
    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}
