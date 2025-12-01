package edu.ufl.cnt4007.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class ServerHandler extends Handler implements Runnable {
  int peerId;
  private Socket socket;
  private PeerClient peerClient;
  private boolean isRegistered = false;

  public ServerHandler(int peerId, Socket socket, PeerClient peerClient) {
    this.peerId = peerId;
    this.socket = socket;
    this.peerClient = peerClient;
  }

  @Override
  public void run() {
    try (OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream()) {
      byte[] handshakeMessage = createHandshakeMessage(peerClient.getMyPeerId());
      out.write(handshakeMessage);
      out.flush();
      System.out.println("[DEBUG] Sent handshake to peer " + peerId);

      this.isRegistered = true;
      peerClient.registerServer(peerId, this);
    } catch (IOException e) {

    } finally {

    }
  }
}
