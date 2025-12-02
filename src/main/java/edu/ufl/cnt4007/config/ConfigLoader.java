package edu.ufl.cnt4007.config;

public class ConfigLoader {
  CommonConfig commonConfig;
  PeerConfig peerConfig;

  public ConfigLoader() throws Exception {
    this.commonConfig = new CommonConfig();
    this.peerConfig = new PeerConfig();
  }

  public CommonConfig getCommonConfig() {
    return commonConfig;
  }

  public PeerConfig getPeerConfig() {
    return peerConfig;
  }
}
