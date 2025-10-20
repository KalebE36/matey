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
    private final PeerConfig peerConfig;
    private final PeerProcess peerProcess;

    private static final long RETRY_INTERVAL_MS = 5000;

    public PeerClient(int myPeerId, PeerConfig peerConfig, PeerProcess peerProcess) {
        this.myPeerId = myPeerId;
        this.peerConfig = peerConfig;
        this.peerProcess = peerProcess;
    }

    @Override
    public void run() {
        System.out.println("[DEBUG] Peer Client thread started. Will attempt to connect to preceding peers.");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                for (Map.Entry<Integer, PeerInfo> entry : peerConfig.getPeerInfoMap().entrySet()) {
                    PeerInfo neighborInfo = entry.getValue();

                    if ((!peerProcess.doesClientExist(neighborInfo.peerId))
                            && (!peerProcess.doesServerExist(neighborInfo.peerId))
                            && (neighborInfo.peerId != myPeerId)) {

                        try {
                            System.out.println("[DEBUG] Attempting to connect to peer " + neighborInfo.peerId);
                            Socket clientSocket = new Socket(neighborInfo.host, neighborInfo.port);

                            System.out.println("[DEBUG] Successfully connected to peer " + neighborInfo.peerId);

                            ServerHandler clientHandler = new ServerHandler(neighborInfo.peerId, clientSocket, this);
                            new Thread(clientHandler).start();

                        } catch (IOException e) {
                            System.out.println(
                                    "[DEBUG] Failed to connect to peer " + neighborInfo.peerId + ". Will retry later.");
                        }
                    }
                }

                // Wait for retyring hosts
                Thread.sleep(RETRY_INTERVAL_MS);

            } catch (InterruptedException e) {
                System.out.println("[EXIT] Peer Connector thread was interrupted. Shutting down.");
                Thread.currentThread().interrupt();
            }
        }
    }

    public PeerProcess getPeerProcess() {
        return peerProcess;
    }

    public void registerServer(int peerId, ServerHandler serverHandler) {
        peerProcess.getRegisteredServers().put(peerId, serverHandler);
    }

    public int getMyPeerId() {
        return myPeerId;
    }
}
