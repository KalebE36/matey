package edu.ufl.cnt4007.config;

public class PeerInfo {
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
