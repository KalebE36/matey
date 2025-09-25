package edu.ufl.cnt4007.net;

// ClientHandler class

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import edu.ufl.cnt4007.core.Message;
import edu.ufl.cnt4007.core.PeerProcess;

public class ClientHandler extends Handler implements Runnable {
    private final Socket clientSocket;
    private final PeerServer server;
    private final PeerProcess peerProcess;
    private boolean isRegistered = false;

    // Constructor
    public ClientHandler(Socket socket, PeerServer server, PeerProcess peerProcess) {
        this.clientSocket = socket;
        this.server = server;
        this.peerProcess = peerProcess;
    }

    @Override
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try {

            // Read from client in bytes
            byte[] inputBytes = clientSocket.getInputStream().readAllBytes();

            if (!isRegistered) {
                // Handle handshake
                getHandleHandshakeMessage(inputBytes, 1, peerProcess);
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