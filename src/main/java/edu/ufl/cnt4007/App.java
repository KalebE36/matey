package edu.ufl.cnt4007;

import java.io.IOException;

import edu.ufl.cnt4007.config.CommonConfig;
import edu.ufl.cnt4007.config.PeerConfig;
import edu.ufl.cnt4007.core.PeerProcess;

public class App {
    public static void main(String[] args) {

        CommonConfig commonCfg;
        PeerConfig peerCfg;

        /* Config */
        try {
            commonCfg = new CommonConfig();
            peerCfg = new PeerConfig();
        } catch (IOException e) {
            System.err.println("Error reading Common.cfg: " + e.getMessage());
            return;
        }

        PeerProcess peerProcess = new PeerProcess(commonCfg, peerCfg, 1);
        peerProcess.start();
    }
}
