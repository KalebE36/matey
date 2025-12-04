package edu.ufl.cnt4007.peer;

import java.util.List;

import edu.ufl.cnt4007.config.ConfigLoader;
import edu.ufl.cnt4007.config.PeerInfo;
import edu.ufl.cnt4007.file.DownloadManager;
import edu.ufl.cnt4007.net.ConnectionHandler;
import edu.ufl.cnt4007.net.Connector;
import edu.ufl.cnt4007.net.Listener;
import edu.ufl.cnt4007.protocol.Bitfield;
import edu.ufl.cnt4007.protocol.Message;

/**
 * This is the main peer process class that manages peer-to-peer interactions.
 *
 * <p>When it runs, it starts listener and connector threads to handle incoming and outgoing
 * connections. This effectively makes each peer both a server and a client in the P2P network.
 * Also, each Listener and Connector will spawn ConnectionHandler threads for each connection. These
 * connections are registered back to this PeerProcess via the PeerContext interface.
 *
 * <p>The Listeners and Connectors implement ConnectionHandler to manage message sending and
 * receiving. ConnectionHandler automatically handles the handshake and bitfield exchange upon
 * connection establishment. However, the PeerProcess must implement the logic for handling messages
 * received from other peers.
 *
 * <p>To send messages to other peers, the PeerProcess can use the registered ConnectionHandler
 * instances within the connections map and attempt to send messages via those handlers.
 *
 * <p>We also keep an internal representation of other peers' bitfields to track which pieces they
 * have. This is crucial for determining interest and requesting pieces.
 *
 * <p>We also keep track of interested peers, choke/unchoke states, and download rates for
 * implementing the choking algorithm and piece selection strategies.
 *
 * <p>Interested in the Bitfield class? Check out its implementation to see how piece availability
 * is managed.
 */
public class PeerProcess implements PeerContext, Runnable {
  private DownloadManager downloadManager;
  private final PeerState peerState;

  @SuppressWarnings("unused")
  private final MessageHandler messageHandler;

  @SuppressWarnings("unused")
  private final ChokingManager chokingManager;

  public PeerProcess(int peerId, ConfigLoader configLoader) throws Exception {
    PeerInfo peerProcessInfo =
        new PeerInfo(
            peerId,
            configLoader.getPeerConfig().getPeerInfo(peerId).getHost(),
            configLoader.getPeerConfig().getPeerInfo(peerId).getPort(),
            configLoader.getPeerConfig().getPeerInfo(peerId).isHasFile());

    List<PeerInfo> allPeers = configLoader.getPeerConfig().getAllPeerInfo();
    Bitfield myBitfield =
        new Bitfield(
            configLoader.getCommonConfig().getFileSize(),
            configLoader.getCommonConfig().getPieceSize(),
            peerProcessInfo.isHasFile());

    try {
      this.downloadManager =
          new DownloadManager(
              peerId,
              configLoader.getCommonConfig().getFileName(),
              configLoader.getCommonConfig().getFileSize(),
              configLoader.getCommonConfig().getPieceSize(),
              myBitfield);
    } catch (Exception e) {
      System.out.println("[DEBUG] Error initializing DownloadManager class");
      throw new Exception("[DEBUG] Error initializing DownloadManager class");
    }

    this.peerState =
        new PeerState(peerProcessInfo, allPeers, myBitfield, configLoader, downloadManager);

    this.chokingManager = new ChokingManager(this.peerState);
    this.messageHandler = new MessageHandler(this.peerState);
  }

  @Override
  public void run() {
    // Implementation for starting listener and connector threads
    Listener listener = new Listener(this, peerState.getPeerProcessInfo().getPort());
    new Thread(listener).start();

    Connector connector = new Connector(this, peerState.getAllPeers());
    new Thread(connector).start();
  }

  @Override
  public int getPeerId() {
    return peerState.getPeerProcessInfo().getPeerId();
  }

  @Override
  public Bitfield getBitfield() {
    return peerState.getMyBitfield();
  }

  @Override
  public void onMessageReceived(int remotePeerId, Message message) {
    messageHandler.onMessage(remotePeerId, message);
  }

  @Override
  public void registerConnection(int remotePeerId, ConnectionHandler handler) {
    peerState.setupConnection(remotePeerId, handler);
  }

  @Override
  public void unregisterConnection(int remotePeerId) {
    peerState.removeConnection(remotePeerId);
  }
}
