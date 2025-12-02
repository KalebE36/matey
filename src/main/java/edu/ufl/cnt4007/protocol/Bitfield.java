package edu.ufl.cnt4007.protocol;

/**
 * Represents a bitfield for tracking piece availability in a P2P file sharing
 * system.
 * 
 * <p>
 * Each bit in the bitfield indicates whether a specific piece of the file is
 * present
 * (1) or absent (0). Internally, the bitfield is stored as a {@code byte[]}
 * array,
 * where each byte contains 8 bits.
 * </p>
 * 
 * <p>
 * Example interpretation:
 * <ul>
 * <li>First byte: 0b10101010 → pieces 0, 2, 4, 6 are present</li>
 * <li>Second byte: 0b00001111 → pieces 8, 9, 10, 11 are present</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Bitfields can be compared using bitwise operations to determine which pieces
 * are available in one bitfield but not the other.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * Bitfield bf1 = new Bitfield(fileSize, pieceSize);
 * Bitfield bf2 = new Bitfield(fileSize, pieceSize);
 * bf1.setPiece(0);
 * bf2.setPiece(1);
 * boolean hasPiece0 = bf1.hasPiece(0); // true
 * boolean hasPiece1 = bf1.hasPiece(1); // false
 * }</pre>
 * 
 */

public class Bitfield {
    private byte[] bitfield;

    public Bitfield(byte[] bitfield) {
        this.bitfield = bitfield;
    }

    public Bitfield(long fileSize, int pieceSize) {
        int totalPieces = (int) Math.ceil((double) fileSize / pieceSize);
        this.bitfield = new byte[(int) Math.ceil((double) totalPieces / 8)];
    }

    public Bitfield(long fileSize, int pieceSize, boolean hasFile) {
        int totalPieces = (int) Math.ceil((double) fileSize / pieceSize);
        this.bitfield = new byte[(int) Math.ceil((double) totalPieces / 8)];
        if (hasFile) {
            setAllPieces(totalPieces);
        }
    }

    public boolean hasPiece(int index) {
        int byteIndex = index / 8;
        int bitIndex = index % 8;
        if (byteIndex >= bitfield.length) {
            return false;
        }
        return (bitfield[byteIndex] & (1 << (7 - bitIndex))) != 0;
    }

    public void setPiece(int index) {
        int byteIndex = index / 8;
        int bitIndex = index % 8;
        if (byteIndex < bitfield.length) {
            bitfield[byteIndex] |= (1 << (7 - bitIndex));
        }
    }

    public int nextMissingPiece() {
        for (int i = 0; i < bitfield.length * 8; i++) {
            if (!hasPiece(i)) {
                return i;
            }
        }
        return -1; // All pieces are present
    }

    /**
     * Set all pieces as present in the bitfield.
     * Mainly used when initializing a seed peer.
     * 
     * @param totalPieces Total number of pieces in the torrent.
     */
    public final void setAllPieces(int totalPieces) {
        for (int i = 0; i < totalPieces; i++) {
            setPiece(i);
        }
    }

    public byte[] bytes() {
        return bitfield;
    }

    public static Bitfield fromMessage(Message message) {
        return new Bitfield(message.payload());
    }
}
