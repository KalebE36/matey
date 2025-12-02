package edu.ufl.cnt4007.net;

import edu.ufl.cnt4007.peer.PeerContext;
import edu.ufl.cnt4007.protocol.Handshake;
import edu.ufl.cnt4007.protocol.Message;
import edu.ufl.cnt4007.protocol.MessageIO;
import edu.ufl.cnt4007.protocol.MessageType;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
  private final Socket socket;
  private final PeerContext peerContext;
  private DataInputStream in;
  private DataOutputStream out;
  private int remotePeerId;

  public ConnectionHandler(Socket socket, PeerContext peerContext) {
    this.socket = socket;
    this.peerContext = peerContext;
  }

  @Override
  public void run() {
    try {
      in = new DataInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());

      performHandshake();

      peerContext.registerConnection(remotePeerId, this);

      sendBitfield();

      while (true) {
        Message msg = MessageIO.read(in);
        peerContext.onMessageReceived(remotePeerId, msg);
      }

    } catch (IOException e) {
      System.err.println("[ERROR] ConnectionHandler IO error: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      System.err.println("[ERROR] ConnectionHandler argument error: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("[ERROR] ConnectionHandler unexpected error: " + e.getMessage());
    } finally {
      cleanup();
    }
  }

  public synchronized void send(Message msg) throws IOException {
    MessageIO.write(out, msg);
  }

  public void performHandshake() throws IOException {
    // Send handshake
    out.write(Handshake.build(peerContext.getPeerId()));
    out.flush();

    // Receive handshake
    byte[] handshakeBuffer = new byte[32];
    in.readFully(handshakeBuffer);

    remotePeerId = Handshake.parse(handshakeBuffer);
    if (remotePeerId < 0) {
      throw new IOException("Invalid handshake received");
    }

    System.out.println("[INFO] Handshake completed with peer " + remotePeerId);
  }

  private void sendBitfield() throws IOException {
    byte[] bf = peerContext.getBitfield().bytes();
    if (bf.length == 0) return;

    MessageIO.write(out, new Message(MessageType.BITFIELD, bf));
  }

  private void cleanup() {
    try {
      if (in != null) in.close();
      if (out != null) out.close();
      if (socket != null && !socket.isClosed()) socket.close();
    } catch (IOException ignored) {
    }

    peerContext.unregisterConnection(remotePeerId);

    System.out.println("[INFO] ConnectionHandler for peer " + remotePeerId + " cleaned up.");
  }
}
