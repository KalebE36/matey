package edu.ufl.cnt4007.net;

// ClientHandler class

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import edu.ufl.cnt4007.core.Message;
import java.util.Optional;

public class ClientHandler extends Handler implements Runnable {
    private final Socket clientSocket;
    private final PeerServer server;
    private boolean isRegistered = false;

    // Constructor
    public ClientHandler(Socket socket, PeerServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try {

            // Read from client in bytes
            byte[] inputBytes = clientSocket.getInputStream().readAllBytes();

            if (!isRegistered) {
                int peerId = getHandleHandshakeMessage(inputBytes, this.server, this);
                System.out.println("[DEBUG] Successfully parsed handshake from " + peerId);
            } else {

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}