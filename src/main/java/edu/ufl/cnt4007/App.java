package edu.ufl.cnt4007;

import edu.ufl.cnt4007.core.PeerProcess;

public class App {
  public static void main(String[] args) {

    try {
      PeerProcess peerProcess = new PeerProcess(Integer.parseInt(args[0]));
      peerProcess.start();
    } catch (Exception e) {
      System.err.println("Invalid argument");
      return;
    }
  }
}
