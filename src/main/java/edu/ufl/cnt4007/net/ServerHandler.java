package edu.ufl.cnt4007.net;

import java.net.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import edu.ufl.cnt4007.config.PeerConfig.PeerInfo;

public class ServerHandler extends Handler implements Runnable {
    private Socket socket;
    private PeerClient peerClient;
    private boolean isRegistered = false;

    public ServerHandler(Socket socket, PeerClient peerClient) {
        this.socket = socket;
        this.peerClient = peerClient;
    }

    @Override
    public void run() {
        try (OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream()) {
            byte[] handshakeMessage = createHandshakeMessage(peerClient.getPeerId());
            out.write(handshakeMessage);
            out.flush();
            System.out.println("[DEBUG] Sent handshake to peer " + peerClient.getPeerId());

            this.isRegistered = true;
            peerClient.registerServer(peerClient.getPeerId(), this);
        } catch (IOException e) {

        } finally {

        }
    }

}
