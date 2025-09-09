package edu.ufl.cnt4007.file;

import java.util.Arrays;

public class Bitfield {
    private final boolean[] bitfield;

    public Bitfield(long fileSize, int pieceSize, boolean hasFile) {
        int numPieces = (int) Math.ceil((double) fileSize / pieceSize);
        bitfield = new boolean[numPieces];
        if (hasFile)
            Arrays.fill(bitfield, true);
    }

    public synchronized boolean hasPiece(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= bitfield.length) {
            throw new IndexOutOfBoundsException("Piece index out of bounds");
        }
        return bitfield[index];
    }

    public synchronized void setPieceReceived(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= bitfield.length) {
            throw new IndexOutOfBoundsException("Piece index out of bounds");
        }
        this.bitfield[index] = true;
    }

    public synchronized boolean isComplete() {
        for (boolean hasPiece : bitfield) {
            if (!hasPiece) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean[] getBitfield() {
        return this.bitfield;
    }
}
