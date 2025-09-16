package edu.ufl.cnt4007.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class PeerServer {
    ServerSocket serverSocket;

    // Threadsafe mapping of PeerId to ClientHandler
    ConcurrentHashMap<Integer, ClientHandler> registeredClients = new ConcurrentHashMap<>();

    public PeerServer(int port) throws Exception {
        try {
            this.serverSocket = new ServerSocket(port);
            this.serverSocket.setReuseAddress(true);
            System.out.println("Server listening on port: " + port);

            // running infinite loop for getting
            // client request
            while (true) {

                // socket object to receive incoming client
                // requests
                Socket client = this.serverSocket.accept();

                // Displaying that new client is connected
                // to server
                System.out.println("New client connected"
                        + client.getInetAddress()
                                .getHostAddress());

                // create a new thread object
                ClientHandler clientSock = new ClientHandler(client, this);

                // This thread will handle the client
                // separately
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (this.serverSocket != null) {
                try {
                    this.serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void registerClient(int peerId, ClientHandler clientHandler) {
        this.registeredClients.put(peerId, clientHandler);
    }

    // Not sure if this is safe or not
    public ConcurrentHashMap<Integer, ClientHandler> getRegisteredClients() {
        return this.registeredClients;
    }
}
