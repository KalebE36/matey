package edu.ufl.cnt4007.net;

// ClientHandler class

import edu.ufl.cnt4007.core.Message;
import edu.ufl.cnt4007.core.Message.MessageType;
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

      int peerId = getHandleHandshakeMessage(handshakeBuffer);

      if (peerId != -1) {
        System.out.println("[DEBUG // SERVER] Handshake verified from Peer " + peerId);
        server.registerClient(peerId, this);

        byte[] myHandshake = createHandshakeMessage(server.getMyPeerId());
        out.write(myHandshake);
        out.flush();

        byte[] bitfieldBytes = server.getPeerProcess().getMyPeer().getBitfield().getBytes();

        if (bitfieldBytes != null && bitfieldBytes.length > 0) {
          Message bitfieldMessage = new Message(MessageType.BITFIELD, bitfieldBytes);
          sendMessage(out, bitfieldMessage);

          System.out.println("[DEBUG // SERVER] Sent BITFIELD to Peer " + peerId);
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

          Message message = new Message(type, payload);
          handleMessage(message, peerId);
        }
      }
    } catch (IOException e) {
      System.err.println("[ERROR // SERVER] Connection error " + e.getMessage());
    } finally {

    }
  }
}
