package edu.ufl.cnt4007.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerConfig {
  private final Map<Integer, PeerInfo> peerInfoMap = new HashMap<>();

  public PeerConfig() throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader("cfg/PeerInfo.cfg"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) continue;

        String[] parts = line.split("\\s+");
        if (parts.length != 4) continue; // skip invalid lines

        int peerId = Integer.parseInt(parts[0]);
        String host = parts[1];
        int port = Integer.parseInt(parts[2]);
        boolean hasFile = parts[3].equals("1"); // 1 = has file, 0 = no file

        peerInfoMap.put(peerId, new PeerInfo(peerId, host, port, hasFile));
      }
    }
  }

  public int getNumberOfPeers() {
    return peerInfoMap.size();
  }

  public Map<Integer, PeerInfo> getPeerInfoMap() {
    return peerInfoMap;
  }

  public PeerInfo getPeerInfo(int peerId) {
    return peerInfoMap.get(peerId);
  }

  public List<PeerInfo> getAllPeerInfo() {
    return List.copyOf(peerInfoMap.values());
  }

  public void setPeerInfo(int peerId, PeerInfo info) {
    peerInfoMap.put(peerId, info);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Number of peers: ").append(getNumberOfPeers()).append("\n");
    for (PeerInfo info : peerInfoMap.values()) {
      sb.append(info.getPeerId())
          .append(" ")
          .append(info.getHost())
          .append(" ")
          .append(info.getPort())
          .append(" ")
          .append(info.isHasFile() ? "1" : "0") // match cfg format
          .append("\n");
    }
    return sb.toString();
  }
}
