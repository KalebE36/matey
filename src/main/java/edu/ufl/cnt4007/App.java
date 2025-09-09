package edu.ufl.cnt4007;

import java.io.IOException;

import edu.ufl.cnt4007.config.CommonConfig;
import edu.ufl.cnt4007.config.PeerConfig;
import edu.ufl.cnt4007.core.PeerManager;

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

        PeerManager peerManager = new PeerManager(peerCfg, commonCfg);

        System.out.println(peerManager.isPeerComplete(1001));
    }
}
