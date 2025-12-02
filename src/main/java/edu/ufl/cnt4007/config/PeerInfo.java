package edu.ufl.cnt4007.config;

import lombok.Data;

@Data
public class PeerInfo {
  private int peerId;
  private String host;
  private int port;
  private boolean hasFile;

  public PeerInfo(int peerId, String host, int port, boolean hasFile) {
    this.peerId = peerId;
    this.host = host;
    this.port = port;
    this.hasFile = hasFile;
  }
}
