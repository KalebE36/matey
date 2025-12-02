package edu.ufl.cnt4007;

import edu.ufl.cnt4007.config.ConfigLoader;
import edu.ufl.cnt4007.peer.PeerProcess;

/**
 * Main application entry point for the P2P file sharing system. Starts a PeerProcess based on the
 * provided peer ID.
 */
public class App {
  public static void main(String[] args) {
    try {
      ConfigLoader config = new ConfigLoader();
      int peerId = Integer.parseInt(args[0]);
      PeerProcess peerProcess = new PeerProcess(peerId, config);
      new Thread(peerProcess).start();
    } catch (Exception e) {
      System.err.println("Error starting peer process: " + e.getMessage());
    }
  }
}
