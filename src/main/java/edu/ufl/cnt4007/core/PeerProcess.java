package edu.ufl.cnt4007.core;

import java.util.HashMap;
import java.util.Map;

import edu.ufl.cnt4007.file.Bitfield;
import edu.ufl.cnt4007.handlers.ConnectionHandler;

public class PeerProcess {

    private final String myId;
    private PeerManager peerManager;
    private Bitfield bitField;

    private Map<Integer, ConnectionHandler> activeConnections = new HashMap<>();

    public PeerProcess(String myId) {
        this.myId = myId;
    }
}
