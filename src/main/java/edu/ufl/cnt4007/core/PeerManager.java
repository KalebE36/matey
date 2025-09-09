package edu.ufl.cnt4007.core;

import java.util.HashMap;
import java.util.Map;

import edu.ufl.cnt4007.config.CommonConfig;
import edu.ufl.cnt4007.config.PeerConfig;
import edu.ufl.cnt4007.file.Bitfield;

public class PeerManager {
    private final Map<Integer, Peer> peers = new HashMap<>();

    public PeerManager(PeerConfig peerConfig, CommonConfig commonConfig) {
        for (PeerConfig.PeerInfo info : peerConfig.getPeerInfoMap().values()) {
            peers.put(info.peerId, new Peer(info.peerId, info.host, info.port,
                    new Bitfield(commonConfig.getFileSize(), commonConfig.getPieceSize(), info.hasFile)));
        }
    }

    public Peer getPeer(int peerId) {
        return peers.get(peerId);
    }

    public boolean isPeerComplete(int peerId) {
        Peer peer = peers.get(peerId);
        if (peer == null) {
            throw new IllegalArgumentException("Peer ID not found: " + peerId);
        }
        return peer.isComplete();
    }

}
