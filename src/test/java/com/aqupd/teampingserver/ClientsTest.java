package com.aqupd.teampingserver;

import java.io.*;
import java.net.*;
import org.junit.jupiter.api.Test;

public class ClientsTest {
  @Test
  public void test1() {
    try (Socket socket = new Socket("localhost", 13577)) {

      OutputStream output = socket.getOutputStream();
      PrintWriter writer = new PrintWriter(output, true);
      String response;
      do {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        response = reader.readLine();
      } while (!response.equals("DISCON"));

    } catch (UnknownHostException ex) {
      System.out.println("Server not found: " + ex.getMessage());
    } catch (IOException ex) {
      System.out.println("I/O error: " + ex.getMessage());
    }
  }
}
