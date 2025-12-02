package edu.ufl.cnt4007.peer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ufl.cnt4007.config.ConfigLoader;
import edu.ufl.cnt4007.config.PeerInfo;
import edu.ufl.cnt4007.file.DownloadManager;
import edu.ufl.cnt4007.net.ConnectionHandler;
import edu.ufl.cnt4007.net.Connector;
import edu.ufl.cnt4007.net.Listener;
import edu.ufl.cnt4007.protocol.Bitfield;
import edu.ufl.cnt4007.protocol.Message;
import edu.ufl.cnt4007.protocol.MessageType;

/**
 * This is the main peer process class that manages peer-to-peer interactions.
 * 
 * When it runs, it starts listener and connector threads to handle incoming and
 * outgoing connections.
 * This effectively makes each peer both a server and a client in the P2P
 * network.
 * Also, each Listener and Connector will spawn ConnectionHandler threads for
 * each connection.
 * These connections are registered back to this PeerProcess via the PeerContext
 * interface.
 * 
 * The Listeners and Connectors implement ConnectionHandler to manage message
 * sending and receiving.
 * ConnectionHandler automatically handles the handshake and bitfield exchange
 * upon connection establishment.
 * However, the PeerProcess must implement the logic for handling messages
 * received from other peers.
 * 
 * To send messages to other peers, the PeerProcess can use the registered
 * ConnectionHandler instances
 * within the connections map and attempt to send messages via those handlers.
 * 
 * We also keep an internal representation of other peers' bitfields to track
 * which pieces they have.
 * This is crucial for determining interest and requesting pieces.
 * 
 * We also keep track of interested peers, choke/unchoke states, and download
 * rates for implementing
 * the choking algorithm and piece selection strategies.
 * 
 * Interested in the Bitfield class? Check out its implementation to see how
 * piece availability is managed.
 */

public class PeerProcess implements PeerContext, Runnable {
    private final PeerInfo peerProcessInfo;
    private final List<PeerInfo> allPeers;
    private final Bitfield myBitfield;
    private final ConfigLoader configLoader;
    private final DownloadManager downloadManager;

    // Map of peerId -> neighbor's bitfield
    private final Map<Integer, Bitfield> neighborBitfields = new ConcurrentHashMap<>();

    // Map of peerId -> connection handler
    private final Map<Integer, ConnectionHandler> connections = new ConcurrentHashMap<>();

    // Set of interested peerIds
    private final Set<Integer> interestedPeers = ConcurrentHashMap.newKeySet();

    // Map of peerId -> choke/unchoke state
    private final Set<Integer> isChoked = ConcurrentHashMap.newKeySet();

    // Map of peerId -> download rate (bytes/sec)
    private final Map<Integer, Double> downloadRates = new ConcurrentHashMap<>();

    // Timing info for download rate calculation can be added as needed
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public PeerProcess(int peerId, ConfigLoader configLoader) throws Exception {
        this.configLoader = configLoader;
        this.peerProcessInfo = configLoader.getPeerConfig().getPeerInfo(peerId);
        this.allPeers = configLoader.getPeerConfig().getAllPeerInfo();
        this.myBitfield = new Bitfield(configLoader.getCommonConfig().getFileSize(),
                configLoader.getCommonConfig().getPieceSize(), peerProcessInfo.hasFile);
        this.downloadManager = new DownloadManager(
            peerId,
            configLoader.getCommonConfig().getFileName(),
            configLoader.getCommonConfig().getFileSize(),
            configLoader.getCommonConfig().getPieceSize(),
            myBitfield
        );
    }

    @Override
    public void run() {
        // Implementation for starting listener and connector threads

        Listener listener = new Listener(this, peerProcessInfo.port);
        new Thread(listener).start();

        Connector connector = new Connector(this, allPeers);
        new Thread(connector).start();

        // Schedule periodic unchoking task
        scheduleUnchokingTask();
    }

    @Override
    public int getPeerId() {
        return peerProcessInfo.peerId;
    }

    @Override
    public Bitfield getBitfield() {
        return myBitfield;
    }

    @Override
    public void onMessageReceived(int remotePeerId, Message message) {
        switch (message.type()) {
            case CHOKE -> handleChokeMessage(remotePeerId);
            case UNCHOKE -> handleUnchokeMessage(remotePeerId);
            case INTERESTED -> handleInterestedMessage(remotePeerId);
            case NOT_INTERESTED -> handleNotInterestedMessage(remotePeerId);
            case HAVE -> handleHaveMessage(remotePeerId, message);
            case BITFIELD -> handleBitfieldMessage(remotePeerId, message);
            case REQUEST -> handleRequestMessage(remotePeerId, message);
            case PIECE -> handlePieceMessage(remotePeerId, message);
            default -> System.out.println("Received unknown message type from peer " + remotePeerId);
        }
    }

    @Override
    public void registerConnection(int remotePeerId, ConnectionHandler handler) {
        connections.put(remotePeerId, handler);
        isChoked.add(remotePeerId);
        downloadRates.put(remotePeerId, 0.0);
    }

    @Override
    public void unregisterConnection(int remotePeerId) {
        connections.remove(remotePeerId);
        neighborBitfields.remove(remotePeerId);
        interestedPeers.remove(remotePeerId);
        isChoked.remove(remotePeerId);
        downloadRates.remove(remotePeerId);
    }

    private void handleBitfieldMessage(int remotePeerId, Message message) {
        Bitfield neighborBitfield = Bitfield.fromMessage(message);
        neighborBitfields.put(remotePeerId, neighborBitfield);
        System.out.println("[INFO] Updated bitfield for peer " + remotePeerId);

        ConnectionHandler handler = connections.get(remotePeerId);

        if (handler == null) {
            System.err.println("[ERROR] No connection handler found for peer " + remotePeerId);
            return;
        }

        try {
            if (isInterested(remotePeerId)) {
                handler.send(new Message(MessageType.INTERESTED, null));
                System.out.println("[INFO] I am interested in " + remotePeerId + "'s pieces'.");
            } else {
                handler.send(new Message(MessageType.NOT_INTERESTED, null));
                System.out.println("[INFO] I am NOT interested in " + remotePeerId + "'s pieces'.");
            }
        } catch (Exception e) {
            System.err.println(
                    "[ERROR] Failed to send INTERESTED/NOT_INTERESTED to peer " + remotePeerId + ": " + e.getMessage());
        }

    }

    private void handleInterestedMessage(int remotePeerId) {
        interestedPeers.add(remotePeerId);
        System.out.println("[INFO] Peer " + remotePeerId + " registered as INTERESTED.");
    }

    private void handleNotInterestedMessage(int remotePeerId) {
        interestedPeers.remove(remotePeerId);
        System.out.println("[INFO] Peer " + remotePeerId + " deregistered and is now NOT_INTERESTED.");
    }

    private boolean isInterested(int remotePeerId) {
        Bitfield neighborBitfield = neighborBitfields.get(remotePeerId);
        if (neighborBitfield == null)
            return false;

        byte[] myBytes = myBitfield.bytes();
        byte[] neighborBytes = neighborBitfield.bytes();
        int length = Math.min(myBytes.length, neighborBytes.length);

        for (int i = 0; i < length; i++) {
            if ((~myBytes[i] & neighborBytes[i]) != 0) {
                return true; // neighbor has at least one piece we don’t
            }
        }

        // Check extra bytes if neighbor has more pieces than us
        for (int i = length; i < neighborBytes.length; i++) {
            if (neighborBytes[i] != 0)
                return true;
        }

        return false; // no new pieces available
    }

    private void scheduleUnchokingTask() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("[DEBUG] Running choking algorithm...");

                // Select preferred neighbors safely
                Set<Integer> preferredNeighbors = selectPreferredNeighbors();
                System.out.println(preferredNeighbors.isEmpty()
                        ? "[DEBUG] No preferred neighbors selected."
                        : "[DEBUG] Preferred neighbors: " + preferredNeighbors);

                // Print interested peers
                System.out.println(interestedPeers.isEmpty()
                        ? "[DEBUG] No interested peers."
                        : "[DEBUG] Interested peers: " + interestedPeers);

                // Iterate over interested peers
                for (Integer peerId : interestedPeers) {
                    ConnectionHandler handler = connections.get(peerId);
                    if (handler == null) {
                        System.err.println("[ERROR] No connection handler found for peer " + peerId);
                        continue;
                    }

                    boolean shouldBeChoked = !preferredNeighbors.contains(peerId);

                    try {
                        if (shouldBeChoked) {
                            if (isChoked.add(peerId)) { // only send CHOKE if not already choked
                                handler.send(new Message(MessageType.CHOKE, null));
                                System.out.println("[INFO] Choked peer " + peerId);
                            }
                        } else {
                            if (isChoked.remove(peerId)) { // only send UNCHOKE if previously choked
                                handler.send(new Message(MessageType.UNCHOKE, null));
                                System.out.println("[INFO] Unchoked peer " + peerId);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[ERROR] Failed to send CHOKE/UNCHOKE to peer " + peerId + ": " + e);
                    }
                }
            } catch (Exception e) {
                // Catch anything that escapes and prevent scheduler from dying
                System.err.println("[ERROR] Exception in choking task: " + e);
                e.printStackTrace();
            }
        }, 0, configLoader.getCommonConfig().getUnchokingInterval(), TimeUnit.SECONDS);
    }

    private Set<Integer> selectPreferredNeighbors() {
        // Sort by download rate (default 0.0)
        // Return top N preferred neighbors (based on Config)
        List<Integer> candidates = new ArrayList<>(interestedPeers);
        candidates
                .sort((a, b) -> Double.compare(downloadRates.getOrDefault(b, 0.0), downloadRates.getOrDefault(a, 0.0)));

        Set<Integer> preferred = new HashSet<>(candidates.subList(0,
                Math.min(configLoader.getCommonConfig().getNumberOfPreferredNeighbors(), candidates.size())));

        return preferred;
    }


    private void handleChokeMessage(int remotePeerId){
        System.out.println("[INFO] Peer " + remotePeerId + " choked us");
        isChoked.add(remotePeerId);
    }

    private void handleUnchokeMessage(int remotePeerId){
        System.out.println("[INFO] Peer " + remotePeerId + " unchoked us");
        isChoked.remove(remotePeerId);
        requestNextPiece(remotePeerId);
    }

    private void handleHaveMessage(int remotePeerId, Message message){
        if(message.payload().length != 4){
            System.err.println("[ERROR] Invalid HAVE message from peer " + remotePeerId);
            return;
        }

        int pieceIndex = java.nio.ByteBuffer.wrap(message.payload()).getInt();
        System.out.println("[INFO] Peer " + remotePeerId + " has piece " + pieceIndex);

        Bitfield neighborBitfield = neighborBitfields.get(remotePeerId);
        if(neighborBitfield != null){
            neighborBitfield.setPiece(pieceIndex);
        }

        ConnectionHandler handler = connections.get(remotePeerId);
        if(handler == null) return;

        try{
           if(isInterested(remotePeerId) && !interestedPeers.contains(remotePeerId)){
            handler.send(new Message(MessageType.INTERESTED, null));
           }
        }

        catch(Exception e){
            System.err.println("[ERROR] Failed to send INTERESTED: " + e.getMessage());
        }
    }

    private void handleRequestMessage(int remotePeerId, Message message){
        if(message.payload().length != 4) return;

        int pieceIndex = java.nio.ByteBuffer.wrap(message.payload()).getInt();

        if(isChoked.contains(remotePeerId)){
            System.out.println("[WARN] Ignoring request from choked peer " + remotePeerId);
            return;
        }

        try{
            byte[] pieceData = downloadManager.readPiece(pieceIndex);
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(4 + pieceData.length);
            buffer.putInt(pieceIndex);
            buffer.put(pieceData);

            ConnectionHandler handler = connections.get(remotePeerId);
            if(handler != null){
                handler.send(new Message(MessageType.PIECE, buffer.array()));
            }
        }

        catch(Exception e){
            System.err.println("[ERROR] Failed to send piece: " + e.getMessage());
        }
    }

    private void handlePieceMessage(int remotePeerId, Message message){
        if(message.payload().length < 4) return;

        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(message.payload());
        int pieceIndex = buffer.getInt();
        byte[] pieceData = new byte[buffer.remaining()];
        buffer.get(pieceData);

        try{
            downloadManager.writePiece(pieceIndex, pieceData);
            broadcastHaveMessage(pieceIndex);

            if(!downloadManager.isComplete() && !isChoked.contains(remotePeerId)){
                requestNextPiece(remotePeerId);
            }
        }

        catch(Exception e){
            System.err.println("[ERROR] Failed to save piece: " + e.getMessage());
        }
    }

    private void requestNextPiece(int remotePeerId){
        Bitfield neighborBitfield = neighborBitfields.get(remotePeerId);
        if(neighborBitfield == null) return;

        List<Integer> availablePieces = new ArrayList<>();
        for(int i = 0; i < downloadManager.getTotalPieces(); i++){
            if(neighborBitfield.hasPiece(i) && !myBitfield.hasPiece(i)){
                availablePieces.add(i);
            }
        }

        if(availablePieces.isEmpty()) return;

        int pieceIndex = availablePieces.get((int)(Math.random() * availablePieces.size()));

        try{
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(4);
            buffer.putInt(pieceIndex);

            ConnectionHandler handler = connections.get(remotePeerId);
            if(handler != null){
                handler.send(new Message(MessageType.REQUEST, buffer.array()));
            }
        }

        catch(Exception e){
            System.err.println("[ERROR] Failed to request piece: " + e.getMessage());
        }
    }

    private void broadcastHaveMessage(int pieceIndex){
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(4);
        buffer.putInt(pieceIndex);
        Message haveMessage = new Message(MessageType.HAVE, buffer.array());

        for(ConnectionHandler handler : connections.values()){
            try{
                handler.send(haveMessage);
            }

            catch(Exception e){
                System.err.println("[ERROR] Failed to broadcast HAVE: " + e.getMessage());
            }
        }
    }
}
