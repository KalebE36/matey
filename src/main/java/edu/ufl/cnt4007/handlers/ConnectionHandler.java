package edu.ufl.cnt4007.handlers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Runnable;
import java.net.Socket;

public class ConnectionHandler implements Runnable {

    private final Socket socket;
    private final String remoteId;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    // Connection Handler Init
    ConnectionHandler(Socket socket, String remoteId) {
        this.socket = socket;
        this.remoteId = remoteId;
    }

    @Override
    public void run() {

    }
}
