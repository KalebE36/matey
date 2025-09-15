package edu.ufl.cnt4007.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import edu.ufl.cnt4007.config.CommonConfig;
import edu.ufl.cnt4007.config.PeerConfig;
import edu.ufl.cnt4007.config.PeerConfig.PeerInfo;
import edu.ufl.cnt4007.file.Bitfield;
import edu.ufl.cnt4007.handlers.ConnectionHandler;

public class PeerProcess {

    private final CommonConfig commonConfig;
    private final PeerConfig peerConfig;
    private Peer myPeer;

    private Map<Integer, ConnectionHandler> activeConnections = new HashMap<>();

    public PeerProcess(CommonConfig commonConfig, PeerConfig peerConfig, int peerId) {
        this.commonConfig = commonConfig;
        this.peerConfig = peerConfig;
        this.myPeer = new Peer(peerId, peerConfig.getPeerInfo(peerId).host, peerConfig.getPeerInfo(peerId).port,
                new Bitfield(commonConfig.getFileSize(), commonConfig.getPieceSize(),
                        peerConfig.getPeerInfo(peerId).hasFile));
    }

    public void start() {
        System.out.println("Starting process");
        startListener();
    }

    public void setConnection(int remoteId, ConnectionHandler connection) {
        // TODO Error handling
        addConnection(remoteId, connection);
    }

    // Listening for incoming TCP connections
    // TODO Need error handling
    private void startListener() {
        new Thread(() -> {
            try (ServerSocket listener = new ServerSocket(peerConfig.getPeerInfo(myPeer.getPeerId()).port);) {

                System.out.println(
                        "Peer " + myPeer.getPeerId() + " is listening on port " + peerConfig.getPeerInfo(this.myPeer.getPeerId()).port);
                while (!Thread.currentThread().isInterrupted()) {
                    Socket newConnection = listener.accept();

                    ConnectionHandler connection = new ConnectionHandler(newConnection, this);
                    new Thread(connection).start();
                }

            } catch (IOException e) {
                System.err.println("\"Peer \" + myPeerId + \" failed to start listener: \" + e.getMessage()");

            }
        }).start();
    }

    // Making TCP connections
    private void startSender(int peerId) {
        if (peerId == myPeer.getPeerId()) {
            System.err.println("Cannot connect to self");
            return;
        }

        PeerInfo peerInfo = peerConfig.getPeerInfo(peerId);

        if (peerInfo == null) {
            System.err.println("Peer ID not found in config");
            return;
        }

        new Thread(() -> {
            try {

                Socket sender = new Socket(peerInfo.host, peerInfo.port);

            } catch (Exception e) {
                //
            }
        }).start();
    }

    private void addConnection(int remoteId, ConnectionHandler connection) {
        activeConnections.put(remoteId, connection);
    }

}
