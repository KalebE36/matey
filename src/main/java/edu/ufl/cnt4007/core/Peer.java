package edu.ufl.cnt4007.core;

import edu.ufl.cnt4007.file.Bitfield;

public class Peer {

  private int peerId;
  private String host;
  private int port;
  private Bitfield bitfield;

  // Contructor
  public Peer(int peerId, String host, int port, Bitfield bitfield) {
    this.peerId = peerId;
    this.host = host;
    this.port = port;
    this.bitfield = bitfield;
  }

  public int getPeerId() {
    return this.peerId;
  }

  public String getHost() {
    return this.host;
  }

  public int getPort() {
    return this.port;
  }

  public boolean isBitfieldComplete() {
    return this.bitfield.isComplete();
  }
}
