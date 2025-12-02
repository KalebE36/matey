package edu.ufl.cnt4007.peer;

import edu.ufl.cnt4007.net.ConnectionHandler;
import edu.ufl.cnt4007.protocol.Bitfield;
import edu.ufl.cnt4007.protocol.Message;

// Your PeerManager or similar class should implement this interface using @Override annotations.
public interface PeerContext {
  int getPeerId();

  Bitfield getBitfield();

  void onMessageReceived(int remotePeerId, Message message);

  void registerConnection(int remotePeerId, ConnectionHandler handler);

  void unregisterConnection(int remotePeerId);
}
