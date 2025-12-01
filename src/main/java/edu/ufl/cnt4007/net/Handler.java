package edu.ufl.cnt4007.net;

import edu.ufl.cnt4007.core.Message;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Handler {

  private int handleHandshakeMessage(byte[] messageBytes) {
    // Check if handshake is valid length (32 bytes)
    if (messageBytes.length != 32) {
      System.out.println("Invalid handshake length");
      return -1;
    }

    // Check the handshake header (18 bytes)
    String header = new String(messageBytes, 0, 18);
    if (!header.equals("P2PFILESHARINGPROJ")) {
      System.out.println("Invalid handshake header");
      return -1;
    }

    // Extract peer ID (last 4 bytes)
    ByteBuffer buffer = ByteBuffer.wrap(messageBytes, 28, 4);
    int peerId = buffer.getInt();

    // Register either a server or a client connection

    System.out.println("[TESTING] SERVER REGISTERED CLIENT: " + peerId);

    return peerId;
  }

  protected void sendMessage(DataOutputStream out, Message msg) throws IOException {
    out.write(msg.toBytes());
    out.flush();
  }

  protected void handleMessage(Message message, int peerId) {
    switch (message.getType()) {
      case CHOKE -> System.out.println("[INFO] Peer " + peerId + " choked me.");
      case UNCHOKE -> System.out.println("[INFO] Peer " + peerId + " unchoked me.");
      case INTERESTED -> System.out.println("[INFO] Peer " + peerId + " is interested.");
      case NOT_INTERESTED -> System.out.println("[INFO] Peer " + peerId + " is not interested.");
      case HAVE -> System.out.println("[INFO] Peer " + peerId + " sent HAVE.");
      case BITFIELD -> handleBitfield(message, peerId);
      case REQUEST -> System.out.println("[INFO] Peer " + peerId + " requested a piece.");
      case PIECE -> System.out.println("[INFO] Peer " + peerId + " sent a piece.");
      default -> System.out.println("[WARN] Unknown message type from " + peerId);
    }
  }

  private void handleBitfield(Message message, int peerId) {
    System.out.println("[INFO] Received BITFIELD from Peer " + peerId);
    // TODO: Logic to compare this bitfield with ours and send 'INTERESTED' or 'NOT INTERESTED'
  }

  public int getHandleHandshakeMessage(byte[] messageBytes) {
    int peerId = handleHandshakeMessage(messageBytes);
    return peerId;
  }

  public byte[] createHandshakeMessage(int myPeerId) {
    ByteBuffer buffer = ByteBuffer.allocate(32);

    buffer.put("P2PFILESHARINGPROJ".getBytes(StandardCharsets.US_ASCII));

    buffer.put(new byte[10]);

    buffer.putInt(myPeerId);

    return buffer.array();
  }
}
