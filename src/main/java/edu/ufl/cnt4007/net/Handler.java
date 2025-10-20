package edu.ufl.cnt4007.net;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import edu.ufl.cnt4007.core.Message;
import edu.ufl.cnt4007.core.PeerProcess;

public class Handler {

    private int handleHandshakeMessage(byte[] messageBytes,
            PeerServer server, ClientHandler clientHandler) {
        // Check if handshake is valid length (32 bytes)
        if (messageBytes.length != 32) {
            System.out.println("Invalid handshake length");
            return -1;
        }

        // Check the handshake header (18 bytes)
        String header = new String(messageBytes, 0, 18);
        if (!header.equals("P2PFILESHARINGPROJ")) {
            System.out.println("Invalid handshake header");
            return -1;
        }

        // Extract peer ID (last 4 bytes)
        ByteBuffer buffer = ByteBuffer.wrap(messageBytes, 28, 4);
        int peerId = buffer.getInt();

        // Register either a server or a client connection

        server.registerClient(peerId, clientHandler);
        System.out.println("[TESTING] SERVER REGISTERED CLIENT: " + peerId);

        return peerId;
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

    public int getHandleHandshakeMessage(byte[] messageBytes, PeerServer server, ClientHandler clientHandler) {

        int peerId = handleHandshakeMessage(messageBytes, server, clientHandler);
        if (peerId != -1) {
            System.out.println(
                    "[DEBUG] Handshake successful  with peer ID: " + peerId);
        }
        return peerId;
    }

    public void getHandleMessage(byte[] messageBytes) {
        handleMessage(messageBytes);
    }

    public byte[] createHandshakeMessage(int myPeerId) {
        ByteBuffer buffer = ByteBuffer.allocate(32);

        buffer.put("P2PFILESHARINGPROJ".getBytes(StandardCharsets.US_ASCII));

        buffer.put(new byte[10]);

        buffer.putInt(myPeerId);

        return buffer.array();
    }

}
