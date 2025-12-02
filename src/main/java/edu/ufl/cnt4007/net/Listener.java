package edu.ufl.cnt4007.net;

import java.net.ServerSocket;

import edu.ufl.cnt4007.peer.PeerContext;

/**
 * Listener is responsible for accepting incoming connections from other peers.
 * For each accepted connection, it spawns a new ConnectionHandler to manage communication.
 */
public class Listener implements Runnable {
    private final PeerContext peerContext;
    private final int port;

    public Listener(PeerContext peerContext, int port) {
        this.peerContext = peerContext;
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[DEBUG] Listener started on port " + port);

            while (!Thread.currentThread().isInterrupted()) {
                ConnectionHandler connectionHandler =
                        new ConnectionHandler(serverSocket.accept(), peerContext);
                new Thread(connectionHandler).start();
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Listener error: " + e.getMessage());
        }
    }
}
