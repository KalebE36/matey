package edu.ufl.cnt4007.protocol;

// -------------------
// MessageType Enum
// -------------------
public enum MessageType {
    CHOKE(0),
    UNCHOKE(1),
    INTERESTED(2),
    NOT_INTERESTED(3),
    HAVE(4),
    BITFIELD(5),
    REQUEST(6),
    PIECE(7);

    private static final MessageType[] LOOKUP;

    private final int value;

    static {
        LOOKUP = new MessageType[values().length];
        for (MessageType type : values()) {
            LOOKUP[type.value] = type;
        }
    }

    MessageType(int value) {
        this.value = value;
    }

    public static MessageType fromValue(int value) {
        if (value < 0 || value >= LOOKUP.length) {
            throw new IllegalArgumentException("Invalid message type value: " + value);
        }
        return LOOKUP[value];
    }

    public int value() {
        return value;
    }
}