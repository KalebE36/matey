package edu.ufl.cnt4007.net;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import edu.ufl.cnt4007.config.PeerInfo;
import edu.ufl.cnt4007.peer.PeerContext;

/**
 * Connector is responsible for establishing outgoing connections to peers
 * with lower peer IDs to avoid duplicate connections.
 * 
 * For each successful connection, it spawns a new ConnectionHandler to manage communication.
 */
public class Connector implements Runnable {
    private final PeerContext peerContext;
    private final List<PeerInfo> peerList;

    public Connector(PeerContext peerContext, List<PeerInfo> peerList) {
        this.peerContext = peerContext;
        this.peerList = peerList;
    }

    @Override
    public void run() {
        for (PeerInfo peerInfo : peerList) {
            // Only connect to peers with lower IDs
            // This is because those with higher IDs will connect to us
            // Thus avoiding duplicate connections
            if (peerInfo.getPeerId() < peerContext.getPeerId()) {
                try {
                    Socket socket = new Socket(peerInfo.getHost(), peerInfo.getPort());
                    ConnectionHandler connectionHandler = new ConnectionHandler(socket, peerContext);
                    new Thread(connectionHandler).start();
                } catch (IOException e) {
                    System.err.println("[ERROR] Failed to connect to peer " + peerInfo.getPeerId() + ": " + e.getMessage());
                }
            }
        }
    }
}
