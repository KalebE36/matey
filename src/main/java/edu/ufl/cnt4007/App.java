package edu.ufl.cnt4007;

import java.io.IOException;

import edu.ufl.cnt4007.config.CommonConfig;
import edu.ufl.cnt4007.config.PeerConfig;

public class App {
    public static void main(String[] args) {

        CommonConfig commonCfg;
        PeerConfig peerCfg;

        try {
            commonCfg = new CommonConfig();
            peerCfg = new PeerConfig();
        } catch (IOException e) {
            System.err.println("Error reading Common.cfg: " + e.getMessage());
            return;
        }

        System.err.println(commonCfg.toString());
        System.err.println(peerCfg.toString());
    }
}
