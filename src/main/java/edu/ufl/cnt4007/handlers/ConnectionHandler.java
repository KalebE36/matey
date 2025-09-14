package edu.ufl.cnt4007.handlers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Runnable;
import java.net.Socket;

import edu.ufl.cnt4007.core.PeerProcess;

public class ConnectionHandler implements Runnable {

    private final Socket socket;
    private final String remoteId;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private final PeerProcess peerProcess;

    // Connection Handler Incoming Init
    public ConnectionHandler(Socket socket, PeerProcess peerProcess) {
        this.socket = socket;
        this.peerProcess = peerProcess;
    }

    @Override
    public void run() {

    }
}
