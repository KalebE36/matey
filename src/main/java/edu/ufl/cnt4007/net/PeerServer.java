package edu.ufl.cnt4007.net;

import edu.ufl.cnt4007.core.PeerProcess;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer implements Runnable {
  ServerSocket serverSocket;
  private final PeerProcess peerProcess;
  private final int peerId;

  public PeerServer(int peerId, int port, PeerProcess peerProcess) throws IOException {
    this.peerId = peerId;
    this.serverSocket = new ServerSocket(port);
    this.serverSocket.setReuseAddress(true);

    // Need the peerprocess for checking hashmaps
    this.peerProcess = peerProcess;
    System.out.println("Server socket created on port: " + port);
  }

  @Override
  public void run() {
    System.out.println("[DEBUG] Server is now listening for connections...");
    try {
      while (true) {
        Socket client = this.serverSocket.accept();
        System.out.println(
            "[DEBUG] New client connected: " + client.getInetAddress().getHostAddress());
        ClientHandler clientSock = new ClientHandler(client, this);
        new Thread(clientSock).start();
      }
    } catch (IOException e) {
      System.err.println("[ERROR] Server listening loop error: " + e.getMessage());
    } finally {
      try {
        if (this.serverSocket != null && !this.serverSocket.isClosed()) {
          this.serverSocket.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void registerClient(int peerId, ClientHandler clientHandler) {
    peerProcess.getRegisteredClients().put(peerId, clientHandler);
  }

  public PeerProcess getPeerProcess() {
    return peerProcess;
  }

  public int getMyPeerId() {
    return peerId;
  }
}
