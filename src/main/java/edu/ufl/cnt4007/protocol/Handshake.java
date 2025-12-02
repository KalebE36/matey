package edu.ufl.cnt4007.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Handshake {
    private static final byte[] HEADER = "P2PFILESHARINGPROJ".getBytes(StandardCharsets.US_ASCII);

    public static byte[] build(int peerId) {
        ByteBuffer buf = ByteBuffer.allocate(32);
        buf.put(HEADER);
        buf.put(new byte[10]); // 10 reserved bytes
        buf.putInt(peerId);
        return buf.array();
    }

    public static int parse(byte[] handshake) {
        if (handshake.length != 32) return -1;

        String header = new String(handshake, 0, 18, StandardCharsets.US_ASCII);
        if (!header.equals("P2PFILESHARINGPROJ")) return -1;

        return ByteBuffer.wrap(handshake, 28, 4).getInt();
    }
}
