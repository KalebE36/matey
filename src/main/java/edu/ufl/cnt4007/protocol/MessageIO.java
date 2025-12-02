package edu.ufl.cnt4007.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Utility class for reading and writing Message objects to and from data
 * streams.
 * This class provides static methods to handle the serialization and
 * deserialization
 */
public class MessageIO {
    /**
     * Reads a Message from the given DataInputStream.
     * 
     * @param in the DataInputStream to read from
     * @return the Message read from the stream
     * @throws Exception
     */
    public static Message read(DataInputStream in) throws IOException {
        // First read message length
        int messageLength = in.readInt(); // includes 1 byte for type + payload

        // Read message type
        MessageType type = MessageType.fromValue(in.readByte());

        // Payload length = messageLength - 1 (because 1 byte is for type)
        int payloadSize = messageLength - 1;
        byte[] payload = new byte[payloadSize];

        // Read the payload
        in.readFully(payload);

        return new Message(type, payload);
    }

    /**
     * Writes a Message to the given DataOutputStream.
     * 
     * @param out     the DataOutputStream to write to
     * @param message the Message to write
     * @throws Exception
     */
    public static void write(DataOutputStream out, Message message) throws IOException {
        byte[] payload = message.payload();
        int messageLength = 1 + payload.length; // 1 byte for message type + payload

        // write length first
        out.writeInt(messageLength);

        // then write type
        out.writeByte(message.type().value());

        // then write payload
        out.write(payload);

        out.flush();
    }

}
