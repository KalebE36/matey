package edu.ufl.cnt4007.peer;

import edu.ufl.cnt4007.config.ConfigLoader;
import edu.ufl.cnt4007.config.PeerInfo;
import edu.ufl.cnt4007.net.ConnectionHandler;
import edu.ufl.cnt4007.protocol.Bitfield;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PeerState {
  private final PeerInfo peerProcessInfo;
  private final List<PeerInfo> allPeers;
  private final Bitfield myBitfield;
  private final ConfigLoader configLoader;

  // Map of peerId -> neighbor's bitfield
  private final Map<Integer, Bitfield> neighborBitfields = new ConcurrentHashMap<>();

  // Map of peerId -> connection handler
  private final Map<Integer, ConnectionHandler> connections = new ConcurrentHashMap<>();

  // Set of interested peerIds
  private final Set<Integer> interestedPeers = ConcurrentHashMap.newKeySet();

  // Map of peerId -> choke/unchoke state
  private final Set<Integer> isChoked = ConcurrentHashMap.newKeySet();

  // Map of peerId -> download rate (bytes/sec), this is updated periodically
  // through the choking manager
  private final Map<Integer, Double> downloadRates = new ConcurrentHashMap<>();

  // Map of peerId -> total bytes downloaded in the current unchoke interval
  private final Map<Integer, Long> bytesDownloadedInInterval = new ConcurrentHashMap<>();

  public PeerState(
      PeerInfo peerProcessInfo,
      List<PeerInfo> allPeers,
      Bitfield myBitfield,
      ConfigLoader configLoader) {
    this.peerProcessInfo = peerProcessInfo;
    this.allPeers = allPeers;
    this.myBitfield = myBitfield;
    this.configLoader = configLoader;
  }

  public void setupConnection(int peerId, ConnectionHandler handler) {
    connections.put(peerId, handler);
    isChoked.add(peerId);
    downloadRates.put(peerId, 0.0);
  }

  public void removeConnection(int peerId) {
    connections.remove(peerId);
    interestedPeers.remove(peerId);
    isChoked.remove(peerId);
    neighborBitfields.remove(peerId);
    downloadRates.remove(peerId);
  }

  public void updateNeighborBitfield(int peerId, Bitfield bitfield) {
    neighborBitfields.put(peerId, bitfield);
  }

  public Bitfield getNeighborBitfield(int peerId) {
    return neighborBitfields.get(peerId);
  }

  public ConnectionHandler getConnectionHandler(int peerId) {
    return connections.get(peerId);
  }

  public void addInterestedPeer(int peerId) {
    interestedPeers.add(peerId);
  }

  public void removeInterestedPeer(int peerId) {
    interestedPeers.remove(peerId);
  }

  public boolean isPeerChoked(int peerId) {
    return isChoked.contains(peerId);
  }

  public void setPeerChoked(int peerId, boolean choked) {
    if (choked) {
      isChoked.add(peerId);
    } else {
      isChoked.remove(peerId);
    }
  }

  public boolean isHavePiece(int pieceIndex) {
    return myBitfield.hasPiece(pieceIndex);
  }

  public void setHavePiece(int pieceIndex) {
    myBitfield.setPiece(pieceIndex);
  }

  // Update neighbor's bitfield when we get a HAVE message
  public void updateNeighborHavePiece(int peerId, int pieceIndex) {
    Bitfield neighborBitfield = neighborBitfields.get(peerId);
    if (neighborBitfield != null) {
      neighborBitfield.setPiece(pieceIndex);
    }
  }

  public boolean isComplete() {
    return myBitfield.isCompleteFast();
  }

  public void incrementBytesDownloaded(int peerId, long bytes) {
    bytesDownloadedInInterval.merge(peerId, bytes, Long::sum);
  }

  private void resetBytesDownloadedInInterval() {
    bytesDownloadedInInterval.clear();
  }

  /**
   * Calculate download rates for each peer based on bytes downloaded in the interval. The interval
   * is the unchoking interval defined in the config.
   */
  public void calculateDownloadRates() {
    int interval = configLoader.getCommonConfig().getUnchokingInterval();
    for (Map.Entry<Integer, Long> entry : bytesDownloadedInInterval.entrySet()) {
      int peerId = entry.getKey();
      long bytes = entry.getValue();
      double rate = (double) bytes / interval;
      System.out.println(
          "[DEBUG] Peer "
              + peerId
              + " downloaded "
              + bytes
              + " bytes in last "
              + interval
              + " seconds. Rate: "
              + rate
              + " bytes/sec");
      downloadRates.put(peerId, rate);
    }

    resetBytesDownloadedInInterval();
  }
}
