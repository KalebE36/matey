package edu.ufl.cnt4007.net;

import edu.ufl.cnt4007.core.Message;
import edu.ufl.cnt4007.core.Message.MessageType;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

      int responseId = getHandleHandshakeMessage(responseHandshake);

      if (responseId != -1) {
        if (responseId != targetPeerId) {
          throw new Exception("Target and peer ID mismatch");
        }

        System.out.println("[DEBUG // CLIENT] Handshake verified from " + targetPeerId);
        peerClient.registerServer(targetPeerId, this);

        byte[] bitfieldBytes = peerClient.getPeerProcess().getMyPeer().getBitfield().getBytes();
        if (bitfieldBytes != null && bitfieldBytes.length > 0) {
          Message bitfieldMsg = new Message(MessageType.BITFIELD, bitfieldBytes);
          sendMessage(out, bitfieldMsg);
          System.out.println("[DEBUG // CLIENT] Sent BITFIELD to Peer " + targetPeerId);
        }

        while (true) {
          int length = in.readInt();
          byte typeByte = in.readByte();
          MessageType type = Message.getMessageType(typeByte);

          int payloadSize = length - 1;
          byte[] payload = new byte[payloadSize];
          if (payloadSize > 0) {
            in.readFully(payload);
          }

          Message msg = new Message(type, payload);
          handleMessage(msg, targetPeerId);
        }
      }

    } catch (Exception e) {
      System.err.println("[ERROR // CLIENT] Communication error with " + targetPeerId);
    } finally {

    }
  }
}
