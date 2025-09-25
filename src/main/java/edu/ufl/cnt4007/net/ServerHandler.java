package edu.ufl.cnt4007.net;

import java.net.*;

import edu.ufl.cnt4007.config.PeerConfig.PeerInfo;

public class ServerHandler extends Handler implements Runnable {
    private Socket socket;
    private PeerInfo peerInfo;
    private boolean isRegistered = false;

    public ServerHandler(Socket socket, PeerInfo peerInfo) {
        this.socket = socket;
        this.peerInfo = peerInfo;
    }

    public void run() {
        System.out.println("ServerHandler thread started. Will handle incoming connections.");
    }
}
