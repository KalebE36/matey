package edu.ufl.cnt4007.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import edu.ufl.cnt4007.core.PeerProcess;

public class PeerServer implements Runnable {
    ServerSocket serverSocket;
    private final PeerProcess peerProcess;

    // Threadsafe mapping of PeerId to ClientHandler
    ConcurrentHashMap<Integer, ClientHandler> registeredClients = new ConcurrentHashMap<>();

    public PeerServer(int port, PeerProcess peerProcess) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setReuseAddress(true);
        this.peerProcess = peerProcess;
        System.out.println("Server socket created on port: " + port);
    }

    @Override
    public void run() {
        System.out.println("Server is now listening for connections...");
        try {
            while (true) {
                Socket client = this.serverSocket.accept();
                System.out.println("New client connected: " + client.getInetAddress().getHostAddress());
                ClientHandler clientSock = new ClientHandler(client, this, this.peerProcess);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            System.err.println("Server listening loop error: " + e.getMessage());
        } finally {
            try {
                if (this.serverSocket != null && !this.serverSocket.isClosed()) {
                    this.serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerClient(int peerId, ClientHandler clientHandler) {
        this.registeredClients.put(peerId, clientHandler);
    }

    // Not sure if this is safe or not
    public Boolean doesClientExist(int peerId) {
        return this.registeredClients.containsKey(peerId);
    }
}
