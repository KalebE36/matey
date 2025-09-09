package edu.ufl.cnt4007;

import edu.ufl.cnt4007.core.Peer;

public class App {
    public static void main(String[] args) {
        Peer peer = new Peer("xxx", "123.234", 000);

        System.out.println("Hello World!");
        System.out.println("Host: " + peer.getHost());
    }
}
