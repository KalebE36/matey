package edu.ufl.cnt4007.protocol;

import java.util.Arrays;
import java.util.Objects;

public class Message {
    private final MessageType type;
    private final byte[] payload;

    public Message (MessageType type, byte[] payload) {
        this.type = Objects.requireNonNull(type);
        this.payload = payload != null ? payload : new byte[0];
    }

    public MessageType type() {
        return type;
    }

    public byte[] payload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", payloadLength=" + (payload != null ? payload.length : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return type == message.type && java.util.Arrays.equals(payload, message.payload);
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + Arrays.hashCode(payload);
    }

}
