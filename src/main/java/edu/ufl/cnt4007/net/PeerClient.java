package edu.ufl.cnt4007.net;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import edu.ufl.cnt4007.config.PeerConfig;
import edu.ufl.cnt4007.config.PeerConfig.PeerInfo;
import edu.ufl.cnt4007.core.PeerProcess;

public class PeerClient implements Runnable {

    Socket clientSocket;
    private final int myPeerId;
    private final PeerServer peerServer;
    private final PeerConfig peerConfig;
    private final PeerProcess peerProcess;

    private static final long RETRY_INTERVAL_MS = 5000;

    public PeerClient(int myPeerId, PeerServer peerServer, PeerConfig peerConfig, PeerProcess peerProcess) {
        this.myPeerId = myPeerId;
        this.peerServer = peerServer;
        this.peerConfig = peerConfig;
        this.peerProcess = peerProcess;
    }

    @Override
    public void run() {
        System.out.println("Peer Client thread started. Will attempt to connect to preceding peers.");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                for (Map.Entry<Integer, PeerInfo> entry : peerConfig.getPeerInfoMap().entrySet()) {
                    PeerInfo neighborInfo = entry.getValue();

                    if ((!peerServer.doesClientExist(neighborInfo.peerId)) && (neighborInfo.peerId != myPeerId)) {

                        try {
                            System.out.println("Attempting to connect to peer " + neighborInfo.peerId);
                            Socket clientSocket = new Socket(neighborInfo.host, neighborInfo.port);

                            System.out.println("Successfully connected to peer " + neighborInfo.peerId);

                            ClientHandler clientHandler = new ClientHandler(clientSocket, peerServer);
                            new Thread(clientHandler).start();

                        } catch (IOException e) {
                            System.out.println(
                                    "Failed to connect to peer " + neighborInfo.peerId + ". Will retry later.");
                        }
                    }
                }

                // Wait for retyring hosts
                Thread.sleep(RETRY_INTERVAL_MS);

            } catch (InterruptedException e) {
                System.out.println("Peer Connector thread was interrupted. Shutting down.");
                Thread.currentThread().interrupt();
            }
        }
    }
}
