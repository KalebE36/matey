package edu.ufl.cnt4007.core;

public class Message {

    public enum MessageType {
        CHOKE(0),
        UNCHOKE(1),
        INTERESTED(2),
        NOT_INTERESTED(3),
        HAVE(4),
        BITFIELD(5),
        REQUEST(6),
        PIECE(7);

        private final int value;

        MessageType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static MessageType fromValue(int value) {
            for (MessageType type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid message type value: " + value);
        }
    }

    private final MessageType type;
    private final int length;
    private final byte[] payload;

    public Message(MessageType type, int length, byte[] payload) {
        this.type = type;
        this.length = length;
        this.payload = payload;
    }

    public static MessageType getMessageType(int value) {
        return MessageType.fromValue(value);
    }

    public static MessageType getMessageType(byte b) {
        // What if parsing throws exception?
        return getMessageType(Byte.toUnsignedInt(b));
    }

}
