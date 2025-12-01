package edu.ufl.cnt4007.core;

import java.nio.ByteBuffer;

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

    private static MessageType fromValue(int value) {
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

  public Message(MessageType type, byte[] payload) {
    this.type = type;
    this.length = 1 + (payload != null ? payload.length : 0);
    this.payload = payload;
  }

  public MessageType getType() {
    return type;
  }

  public byte[] getPayload() {
    return payload;
  }

  public byte[] toBytes() {
    int payloadSize = (payload != null) ? payload.length : 0;
    ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + payloadSize);

    buffer.putInt(length);
    buffer.put((byte) type.getValue());

    if (payload != null && payloadSize > 0) {
      buffer.put(payload);
    }

    return buffer.array();
  }

  public static MessageType getMessageType(byte b) {
    return MessageType.fromValue(Byte.toUnsignedInt(b));
  }
}
