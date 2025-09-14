package edu.ufl.cnt4007.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import edu.ufl.cnt4007.config.CommonConfig;
import edu.ufl.cnt4007.config.PeerConfig;
import edu.ufl.cnt4007.file.Bitfield;
import edu.ufl.cnt4007.handlers.ConnectionHandler;

public class PeerProcess {

    private final CommonConfig commonConfig;
    private final PeerConfig peerConfig;
    private PeerManager peerManager;
    private Bitfield bitField;
    private int myId;

    private Map<Integer, ConnectionHandler> activeConnections = new HashMap<>();

    public PeerProcess(CommonConfig commonConfig, PeerConfig peerConfig, int myId) {
        this.commonConfig = commonConfig;
        this.peerConfig = peerConfig;
        this.myId = myId;
        peerManager = new PeerManager(peerConfig, commonConfig);
    }

    public void start() {
        System.out.println("Starting process");
    }

    private void startListener() {
        new Thread(() -> {
            try (
                    ServerSocket listener = new ServerSocket(peerConfig.getPeerInfo(myId).port);) {

                System.out.println("Peer " + myId + " is listening on port " + peerConfig.getPeerInfo(myId).port);
                while (!Thread.currentThread().isInterrupted()) {
                    Socket newConnection = listener.accept();
                    // Handle the new connection in a separate thread via ConnectionHandler
                    // The ConnectionHandler will perform the handshake and determine the
                    // remotePeerId
                    new Thread(new ConnectionHandler(newConnection, this)).start();
                }

            } catch (Exception e) {
                // TODO: handle exception
            }
        });
    }
}
