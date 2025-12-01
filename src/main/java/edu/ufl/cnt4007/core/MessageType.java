package edu.ufl.cnt4007.core;

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
