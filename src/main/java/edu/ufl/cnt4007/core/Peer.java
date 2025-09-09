package edu.ufl.cnt4007.core;

public class Peer {

    private String peerId;
    private String host;
    private int port;
    
    // Contructor
    public Peer(String peerId, String host, int port) {
        this.peerId = peerId;
        this.host = host;
        this.port = port;
    }

    public String getPeerId() {
        return this.peerId;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }
}
