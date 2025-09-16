package edu.ufl.cnt4007.core;

import java.io.IOException;
import edu.ufl.cnt4007.config.CommonConfig;
import edu.ufl.cnt4007.config.PeerConfig;
import edu.ufl.cnt4007.file.Bitfield;
import edu.ufl.cnt4007.net.PeerServer;

public class PeerProcess {

    private CommonConfig commonConfig;
    private PeerConfig peerConfig;
    private Peer myPeer;

    public PeerProcess(int peerId) {
        try {
            this.commonConfig = new CommonConfig();
            this.peerConfig = new PeerConfig();
            this.myPeer = new Peer(peerId, peerConfig.getPeerInfo(peerId).host, peerConfig.getPeerInfo(peerId).port,
                    new Bitfield(commonConfig.getFileSize(), commonConfig.getPieceSize(),
                            peerConfig.getPeerInfo(peerId).hasFile));
        } catch (IOException e) {
            System.err.println("Error initializing Peer Process");
            return;
        }

    }

    public void start() {
        System.out.println("Starting process");
        inititalizeServer();

    }

    private void inititalizeServer() {
        try {
            new PeerServer(myPeer.getPort());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
    }

}
