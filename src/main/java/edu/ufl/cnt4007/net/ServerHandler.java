package edu.ufl.cnt4007.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class ServerHandler extends Handler implements Runnable {

  private final int targetPeerId;
  private final Socket socket;
  private final PeerClient peerClient;
  private DataOutputStream out;
  private DataInputStream in;

  public ServerHandler(int targetPeerId, Socket socket, PeerClient peerClient) {
    this.targetPeerId = targetPeerId;
    this.socket = socket;
    this.peerClient = peerClient;
  }

  @Override
  public void run() {
    try {
      out = new DataOutputStream(socket.getOutputStream());
      in = new DataInputStream(socket.getInputStream());

      // Send handshake message
      byte[] handshakeMessage = createHandshakeMessage(peerClient.getMyPeerId());
      out.write(handshakeMessage);
      out.flush();
      System.out.println("[DEBUG // CLIENT] Sent handshake to peer " + targetPeerId);

      // Receive handshake message
      byte[] responseHandshake = new byte[32];
      in.readFully(responseHandshake);

      System.out.println("[DEBUG // CLIENT] Handshake response received from " + targetPeerId);

      peerClient.registerServer(targetPeerId, this);

      while (true) {
        int length = in.readInt();
        byte type = in.readByte();
      }

    } catch (IOException e) {
      System.err.println("[ERROR // CLIENT] Communication error with " + targetPeerId);
    } finally {

    }
  }
}
