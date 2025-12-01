package edu.ufl.cnt4007.file;

import java.util.Arrays;

public class Bitfield {
  private final boolean[] pieces;
  private final int size;

  public Bitfield(long fileSize, int pieceSize, boolean hasFile) {
    this.size = (int) Math.ceil((double) fileSize / pieceSize);
    this.pieces = new boolean[size];
    if (hasFile) {
      Arrays.fill(pieces, true);
    }
  }

  public synchronized boolean hasPiece(int index) throws IndexOutOfBoundsException {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("Piece index out of bounds: " + index);
    }
    return pieces[index];
  }

  public synchronized void setPieceReceived(int index) throws IndexOutOfBoundsException {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("Piece index out of bounds: " + index);
    }
    this.pieces[index] = true;
  }

  public synchronized byte[] getBytes() {
    int byteSize = (int) Math.ceil((double) size / 8);
    byte[] bytes = new byte[byteSize];

    for (int i = 0; i < size; i++) {
      if (pieces[i]) {
        int byteIndex = i / 8;
        int bitIndex = 7 - (i % 8);
        bytes[byteIndex] |= (1 << bitIndex);
      }
    }
    return bytes;
  }

  public synchronized void setFromBytes(byte[] bytes) {
    for (int i = 0; i < size; i++) {
      int byteIndex = i / 8;
      int bitIndex = 7 - (i % 8);

      if (byteIndex < bytes.length) {
        boolean isSet = (bytes[byteIndex] & (1 << bitIndex)) != 0;
        pieces[i] = isSet;
      }
    }
  }

  public synchronized boolean isInterestedIn(Bitfield other) {
    for (int i = 0; i < size; i++) {
      if (other.pieces[i] && !this.pieces[i]) {
        return true;
      }
    }
    return false;
  }

  public synchronized boolean isComplete() {
    for (boolean p : pieces) {
      if (!p) return false;
    }
    return true;
  }

  public int getSize() {
    return size;
  }
}
