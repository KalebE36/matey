package edu.ufl.cnt4007.net;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import edu.ufl.cnt4007.config.PeerConfig;
import edu.ufl.cnt4007.config.PeerConfig.PeerInfo;

public class PeerClient {

    Socket clientSocket;

    public PeerClient(int myPeerId, PeerServer peerServer, PeerConfig peerConfig) { // Added myPeerId

        try {

            // Iterate through all peers defined in the config
            for (Map.Entry<Integer, PeerInfo> entry : peerConfig.getPeerInfoMap().entrySet()) {
                PeerInfo neighborInfo = entry.getValue();

                if (neighborInfo.peerId < myPeerId) {
                    System.out.println(
                            "Attempting to connect to peer " + neighborInfo.peerId + " at " + neighborInfo.host);
                    Socket clientSocket = new Socket(neighborInfo.host, neighborInfo.port);
                    System.out.println("Client connected to: " + neighborInfo.host);

                    ClientHandler clientSock = new ClientHandler(clientSocket);
                    new Thread(clientSock).start();
                }
            }
        } catch (IOException e) {
            System.err.println("Could not connect to a peer: " + e.getMessage());
        }
    }

}
