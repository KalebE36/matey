package edu.ufl.cnt4007.peer;

import edu.ufl.cnt4007.net.ConnectionHandler;
import edu.ufl.cnt4007.protocol.Bitfield;
import edu.ufl.cnt4007.protocol.Message;
import edu.ufl.cnt4007.protocol.MessageType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MessageHandler {
  private final PeerState peerState;

  public MessageHandler(PeerState peerState) {
    this.peerState = peerState;
  }

  public void onMessage(int remotePeerId, Message message) {
    switch (message.type()) {
      case CHOKE -> handleChokeMessage(remotePeerId);
      case UNCHOKE -> handleUnchokeMessage(remotePeerId);
      case INTERESTED -> handleInterestedMessage(remotePeerId);
      case NOT_INTERESTED -> handleNotInterestedMessage(remotePeerId);
      case HAVE -> handleHaveMessage(remotePeerId, message);
      case BITFIELD -> handleBitfieldMessage(remotePeerId, message);
      case REQUEST -> handleRequestMessage(remotePeerId, message);
      case PIECE -> handlePieceMessage(remotePeerId, message);
      default ->
          System.err.println(
              "[ERROR] Unknown message type from peer " + remotePeerId + ": " + message.type());
    }
  }

  /**
   * Helper: Sends a message to a specific peer, handling ConnectionHandler retrieval and network
   * errors.
   */
  private void safelySend(int remotePeerId, Message message, String actionDescription) {
    ConnectionHandler handler = peerState.getConnectionHandler(remotePeerId);
    if (handler == null) {
      System.err.println("[ERROR] No connection handler found for peer " + remotePeerId);
      return;
    }
    try {
      handler.send(message);
      System.out.println(
          "[INFO] Sent " + message.type() + " (" + actionDescription + ") to peer " + remotePeerId);
    } catch (Exception e) {
      System.err.println(
          "[ERROR] Failed to send "
              + message.type()
              + " ("
              + actionDescription
              + ") to peer "
              + remotePeerId
              + ": "
              + e.getMessage());
    }
  }

  /** Helper: Sends INTERESTED or NOT_INTERESTED message based on current state check. */
  private void sendInterestStatus(int remotePeerId) {
    MessageType type;
    String status;

    if (isInterested(remotePeerId)) {
      type = MessageType.INTERESTED;
      status = "INTERESTED";
      System.out.println("[INFO] I am interested in " + remotePeerId + "'s pieces'.");
    } else {
      type = MessageType.NOT_INTERESTED;
      status = "NOT_INTERESTED";
      System.out.println("[INFO] I am NOT interested in " + remotePeerId + "'s pieces'.");
    }

    Message message = new Message(type, null);
    safelySend(remotePeerId, message, status);
  }

  /**
   * Helper: Finds the next missing piece and sends a REQUEST message if available. Sends
   * NOT_INTERESTED if no missing pieces are found.
   */
  private void attemptRequestNextPiece(int remotePeerId) {
    int nextPieceIndex = nextMissingPiece(remotePeerId);

    if (nextPieceIndex == -1) {
      System.out.println(
          "[INFO] No missing pieces to request from peer "
              + remotePeerId
              + ". Sending NOT_INTERESTED.");
      // We are not interested anymore
      safelySend(
          remotePeerId,
          new Message(MessageType.NOT_INTERESTED, null),
          "Became NOT_INTERESTED (no missing pieces).");
      return;
    }

    // Build and send REQUEST Message
    byte[] bytes = ByteBuffer.allocate(4).putInt(nextPieceIndex).array();
    Message requestMessage = new Message(MessageType.REQUEST, bytes);

    safelySend(remotePeerId, requestMessage, "REQUEST for piece " + nextPieceIndex);
  }

  /** Helper: Broadcasts a HAVE message for the given piece index to all connected peers. */
  private void broadcastHave(int pieceIndex) {
    Message haveMessage =
        new Message(MessageType.HAVE, ByteBuffer.allocate(4).putInt(pieceIndex).array());

    for (Map.Entry<Integer, ConnectionHandler> con : peerState.getConnections().entrySet()) {
      int remotePeerId = con.getKey();
      ConnectionHandler handler = con.getValue();
      try {
        handler.send(haveMessage);
        System.out.println("[INFO] Sent HAVE for piece " + pieceIndex + " to peer " + remotePeerId);
      } catch (Exception e) {
        System.err.println(
            "[ERROR] Failed to send HAVE to peer " + remotePeerId + ": " + e.getMessage());
      }
    }
  }

  /** Helper: Broadcasts a NOT_INTERESTED message to all connected peers. */
  private void broadcastNotInterested() {
    Message notInterestedMessage = new Message(MessageType.NOT_INTERESTED, null);

    for (Map.Entry<Integer, ConnectionHandler> con : peerState.getConnections().entrySet()) {
      int remotePeerId = con.getKey();
      ConnectionHandler handler = con.getValue();
      try {
        handler.send(notInterestedMessage);
        System.out.println("[INFO] Sent NOT_INTERESTED to peer " + remotePeerId);
      } catch (Exception e) {
        System.err.println(
            "[ERROR] Failed to send NOT_INTERESTED to peer "
                + remotePeerId
                + ": "
                + e.getMessage());
      }
    }
  }

  // --- Message Handling Methods ---

  private void handleHaveMessage(int remotePeerId, Message message) {
    System.out.println("[INFO] Received HAVE from peer " + remotePeerId);

    // Extract piece index from payload
    int pieceIndex = ByteBuffer.wrap(message.payload()).getInt();
    peerState.updateNeighborHavePiece(remotePeerId, pieceIndex);

    // Send INTERESTED or NOT_INTERESTED based on the new piece
    sendInterestStatus(remotePeerId);
  }

  private void handlePieceMessage(int remotePeerId, Message message) {
    System.out.println("[INFO] Received PIECE from peer " + remotePeerId);

    // First 4 bytes of payload is the piece index
    int pieceIndex = ByteBuffer.wrap(message.payload(), 0, 4).getInt();

    // TODO: Extract piece data and store it locally

    peerState.setHavePiece(pieceIndex);

    // Notify all surrounding peers about the new piece
    broadcastHave(pieceIndex);

    // If I have all pieces now, log completion and stop downloading
    if (peerState.isComplete()) {
      System.out.println("[INFO] I have downloaded all pieces!");
      broadcastNotInterested();
      return;
    }

    // Determine next missing piece to request
    attemptRequestNextPiece(remotePeerId);
  }

  private void handleRequestMessage(int remotePeerId, Message message) {
    System.out.println("[INFO] Received REQUEST from peer " + remotePeerId);

    // Check whether the peer is choked
    if (peerState.isPeerChoked(remotePeerId)) {
      System.out.println("[INFO] Peer " + remotePeerId + " is choked. Ignoring REQUEST.");
      return;
    }

    // Check whether we have the requested piece
    int pieceIndex = ByteBuffer.wrap(message.payload()).getInt();
    if (!peerState.isHavePiece(pieceIndex)) {
      System.out.println("[INFO] We do not have piece " + pieceIndex + ". Ignoring REQUEST.");
      return;
    }

    long pieceSize = peerState.getMyBitfield().getPieceSize(pieceIndex);

    // Send PIECE message (not implemented here)
    System.out.println("[INFO] Sending PIECE " + pieceIndex + " to peer " + remotePeerId);

    // Payload consists of a 4 byte index + the actual piece data
    // Calculate piece size

    // TODO: Add the actual piece data here
    byte[] payload = ByteBuffer.allocate(4 + (int) pieceSize).putInt(pieceIndex).array();

    Message msg = new Message(MessageType.PIECE, payload); // Placeholder for actual piece data
    safelySend(remotePeerId, msg, "PIECE " + pieceIndex);

    // Increment download stats
    peerState.incrementBytesDownloaded(remotePeerId, pieceSize);
  }

  private void handleChokeMessage(int remotePeerId) {
    System.out.println("[INFO] Received CHOKE from peer " + remotePeerId);
    // No further action needed for CHOKE in this implementation
  }

  private void handleUnchokeMessage(int remotePeerId) {
    System.out.println("[INFO] Received UNCHOKE from peer " + remotePeerId);
    // Request the next piece immediately when unchoked
    attemptRequestNextPiece(remotePeerId);
  }

  private void handleBitfieldMessage(int remotePeerId, Message message) {
    Bitfield neighborBitfield =
        Bitfield.fromMessage(
            message,
            peerState.getConfigLoader().getCommonConfig().getFileSize(),
            peerState.getConfigLoader().getCommonConfig().getPieceSize());
    peerState.updateNeighborBitfield(remotePeerId, neighborBitfield);
    System.out.println("[INFO] Updated bitfield for peer " + remotePeerId);

    // Send INTERESTED or NOT_INTERESTED based on the new bitfield
    sendInterestStatus(remotePeerId);
  }

  private boolean isInterested(int remotePeerId) {
    Bitfield neighborBitfield = peerState.getNeighborBitfield(remotePeerId);
    if (neighborBitfield == null) return false;

    byte[] myBytes = peerState.getMyBitfield().bytes();
    byte[] neighborBytes = neighborBitfield.bytes();
    int length = Math.min(myBytes.length, neighborBytes.length);

    for (int i = 0; i < length; i++) {
      // Check if neighbor has a piece we don't: (~myBytes[i] & neighborBytes[i]) != 0
      if ((~myBytes[i] & neighborBytes[i]) != 0) {
        return true;
      }
    }

    // Check extra bytes if neighbor has more pieces than us
    for (int i = length; i < neighborBytes.length; i++) {
      if (neighborBytes[i] != 0) return true;
    }

    return false; // no new pieces available
  }

  // Randomly selects the next missing piece from the neighbor's bitfield
  private int nextMissingPiece(int remotePeerId) {
    Bitfield neighborBitfield = peerState.getNeighborBitfield(remotePeerId);
    if (neighborBitfield == null) return -1;

    int totalPieces = peerState.getMyBitfield().getPieceCount();
    List<Integer> candidates = new ArrayList<>(totalPieces);

    for (int i = 0; i < totalPieces; i++) {
      if (!peerState.getMyBitfield().hasPiece(i) && neighborBitfield.hasPiece(i)) {
        candidates.add(i);
      }
    }

    if (candidates.isEmpty()) return -1;

    // Uniform random pick
    return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
  }

  private void handleInterestedMessage(int remotePeerId) {
    peerState.addInterestedPeer(remotePeerId);
    System.out.println("[INFO] Peer " + remotePeerId + " registered as INTERESTED.");
  }

  private void handleNotInterestedMessage(int remotePeerId) {
    peerState.removeInterestedPeer(remotePeerId);
    System.out.println("[INFO] Peer " + remotePeerId + " deregistered and is now NOT_INTERESTED.");
  }
}
