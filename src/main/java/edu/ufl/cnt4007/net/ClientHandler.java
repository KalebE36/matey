package edu.ufl.cnt4007.net;

// ClientHandler class

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Handler implements Runnable {
  private final Socket socket;
  private final PeerServer server;
  private DataOutputStream out;
  private DataInputStream in;

  // Constructor
  public ClientHandler(Socket socket, PeerServer server) {
    this.socket = socket;
    this.server = server;
  }

  @Override
  public void run() {
    try {
      out = new DataOutputStream(socket.getOutputStream());
      in = new DataInputStream(socket.getInputStream());

      byte[] handshakeBuffer = new byte[32];

      in.readFully(handshakeBuffer);

      int peerId = getHandleHandshakeMessage(handshakeBuffer, this.server, this);

      if (peerId != -1) {
        System.out.println("[DEBUG // SERVER] Handshake verified from Peer " + peerId);
        server.registerClient(peerId, this);

        byte[] myHandshake = createHandshakeMessage(server.getMyPeerId());
        out.write(myHandshake);
        out.flush();
      }
    } catch (IOException e) {
      System.err.println("[ERROR // SERVER] Connection error " + e.getMessage());
    } finally {

    }
  }
}
