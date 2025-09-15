package edu.ufl.cnt4007.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Runnable;
import java.net.Socket;

import edu.ufl.cnt4007.core.PeerProcess;

public class ConnectionHandler implements Runnable {

    private final Socket socket;
    private int remoteId;

    private final PeerProcess peerProcess;

    // Connection Handler Incoming Init
    public ConnectionHandler(Socket socket, PeerProcess peerProcess) {
        this.socket = socket;
        this.peerProcess = peerProcess;
    }

    @Override
    public void run() {
        readBuffer();
        sendBuffer();
    }

    private void readBuffer() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
            String message = in.readLine();
            System.out.println("message: " + message);
        } catch (IOException e) {

        }
    }

    private void sendBuffer() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);) {
            out.println("Something to send back");
        } catch (IOException e) {

        }
    }

    private void sendHandshake() {

    }

    private void recieveHandshake() {

    }
}
