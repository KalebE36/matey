package edu.ufl.cnt4007.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PeerConfig {

  public static class PeerInfo {
    public int peerId;
    public String host;
    public int port;
    public boolean hasFile;

    public PeerInfo(int peerId, String host, int port, boolean hasFile) {
      this.peerId = peerId;
      this.host = host;
      this.port = port;
      this.hasFile = hasFile;
    }
  }

  private int numberOfPeers = 0;
  private Map<Integer, PeerInfo> peerInfoMap = new HashMap<>();

  public PeerConfig() throws IOException {
    try ( // Parse PeerInfo.cfg
    BufferedReader reader = new BufferedReader(new FileReader("cfg/PeerInfo.cfg"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) continue;
        String[] parts = line.split("\\s+", 4);
        if (parts.length == 4) {
          // Parse 0 as int
          int peerId = Integer.parseInt(parts[0]);
          PeerInfo peerInfo =
              new PeerInfo(peerId, parts[1], Integer.parseInt(parts[2]), parts[3].equals("1"));

          peerInfoMap.put(peerId, peerInfo);
          this.numberOfPeers++;
        }
      }
    }
  }

  public int getNumberOfPeers() {
    return numberOfPeers;
  }

  public Map<Integer, PeerInfo> getPeerInfoMap() {
    return peerInfoMap;
  }

  public PeerInfo getPeerInfo(int peerId) {
    return peerInfoMap.get(peerId);
  }

  public void setPeerInfo(int peerId, PeerInfo info) {
    peerInfoMap.put(peerId, info);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("NumberOfPeers: ").append(numberOfPeers).append("\n");
    for (PeerInfo info : peerInfoMap.values()) {
      sb.append(info.peerId)
          .append(" ")
          .append(info.host)
          .append(" ")
          .append(info.port)
          .append(" ")
          .append(info.hasFile ? "yes" : "no")
          .append("\n");
    }
    return sb.toString();
  }
}
