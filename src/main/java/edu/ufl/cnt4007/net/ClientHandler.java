package edu.ufl.cnt4007.net;

// ClientHandler class

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import edu.ufl.cnt4007.core.Message;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private PeerServer server;
    private boolean isRegistered = false;

    private void handleHandshakeMessage(byte[] messageBytes) {
        // Check if handshake is valid length (32 bytes)
        if (messageBytes.length != 32) {
            System.out.println("Invalid handshake length");
            return;
        }

        // Check the handshake header (18 bytes)
        String header = new String(messageBytes, 0, 18);
        if (!header.equals("P2PFILESHARINGPROJ")) {
            System.out.println("Invalid handshake header");
            return;
        }

        // Extract peer ID (last 4 bytes)
        ByteBuffer buffer = ByteBuffer.wrap(messageBytes, 28, 4);
        int peerId = buffer.getInt();

        // Register client
        server.registerClient(peerId, this);
        this.isRegistered = true;
    }

    private void handleMessage(byte[] messageBytes) {
        // Check if message is at least 5 bytes (4 bytes length + 1 byte type)
        if (messageBytes.length < 5) {
            System.out.println("Invalid message length");
            return;
        }

        // Extract message length (first 4 bytes)
        ByteBuffer lengthBuffer = ByteBuffer.wrap(messageBytes, 0, 4);
        int messageLength = lengthBuffer.getInt();

        // Extract message type (5th byte)
        byte messageTypeByte = messageBytes[4];
        Message.MessageType messageType = Message.getMessageType(messageTypeByte);

        // Check if payload length matches message length so (4 + 1 + message length)
        if (messageBytes.length != 4 + 1 + messageLength) {
            System.out.println("Incomplete or malformed message payload");
            return;
        }

        Message message = new Message(messageType, messageLength,
                Arrays.copyOfRange(messageBytes, 5, 5 + messageLength));

        switch (messageType) {
            case Message.MessageType.CHOKE -> System.out.println("Received CHOKE message");
            case Message.MessageType.UNCHOKE -> System.out.println("Received UNCHOKE message");
            case Message.MessageType.INTERESTED -> System.out.println("Received INTERESTED message");
            case Message.MessageType.NOT_INTERESTED -> System.out.println("Received NOT_INTERESTED message");
            case Message.MessageType.HAVE -> System.out.println("Received HAVE message");
            case Message.MessageType.BITFIELD -> System.out.println("Received BITFIELD message");
            case Message.MessageType.REQUEST -> System.out.println("Received REQUEST message");
            case Message.MessageType.PIECE -> System.out.println("Received PIECE message");
            default -> throw new AssertionError();
        }
    }

    // Constructor
    public ClientHandler(Socket socket, PeerServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
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
                handleHandshakeMessage(inputBytes);
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