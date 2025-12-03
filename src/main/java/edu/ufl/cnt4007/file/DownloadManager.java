package edu.ufl.cnt4007.file;

import edu.ufl.cnt4007.protocol.Bitfield;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
 * This DownloadManger class manages file download and upload operations for a peer
 * Also handles reading and writing file pieces to disk
 */

public class DownloadManager {
  private final String baseDir; // "peer_1001"
  private final long fileSize;
  private final int pieceSize;
  private final int totalPieces;
  private final Bitfield bitfield;
  private final RandomAccessFile file;

  public DownloadManager(
      int peerId, String fileName, long fileSize, int pieceSize, Bitfield bitfield)
      throws IOException {
    this.baseDir = "peer_" + peerId;
    this.fileSize = fileSize;
    this.pieceSize = pieceSize;
    this.totalPieces = (int) Math.ceil((double) fileSize / pieceSize);
    this.bitfield = bitfield;

    Files.createDirectories(Paths.get(baseDir));
    File targetFile = new File(baseDir, fileName);
    this.file = new RandomAccessFile(targetFile, "rw");
    if (file.length() == 0) {
      file.setLength(fileSize);
    }
  }

  // Reads a piece from file
  public synchronized byte[] readPiece(int pieceIndex) throws IOException {
    if (!bitfield.hasPiece(pieceIndex)) {
      throw new IOException("Piece " + pieceIndex + " not available");
    }

    long offset = (long) pieceIndex * pieceSize;
    int currentSize = getPieceSize(pieceIndex);

    if (offset + currentSize > fileSize) {
      currentSize = (int) (fileSize - offset);
    }

    byte[] pieceData = new byte[currentSize];
    file.seek(offset);
    file.readFully(pieceData);

    return pieceData;
  }

  // Writes a piece to the file and updates bitfield
  public synchronized void writePiece(int pieceIndex, byte[] data) throws IOException {
    if (bitfield.hasPiece(pieceIndex)) {
      System.out.println("[WARN] Piece " + pieceIndex + " already exists, skipping write");
      return;
    }

    long offset = (long) pieceIndex * pieceSize;
    int expectedSize = getPieceSize(pieceIndex);

    // Validate exact piece size - no truncation allowed
    if (data.length != expectedSize) {
      throw new IOException(
          "Piece size mismatch. Expected: "
              + expectedSize
              + ", got: "
              + data.length
              + ". Piece may be corrupted during transmission.");
    }

    // Write the piece
    file.seek(offset);
    file.write(data);
    file.getFD().sync();

    // Update bitfield after successful write
    bitfield.setPiece(pieceIndex);
  }

  // Gets the size of a specific piece
  public int getPieceSize(int pieceIndex) {
    if (pieceIndex < totalPieces - 1) {
      return pieceSize;
    } else {
      return (int) (fileSize - (long) pieceIndex * pieceSize); // Last piece might be smaller
    }
  }

  // Checks if download is complete
  public boolean isComplete() {
    for (int i = 0; i < totalPieces; i++) {
      if (!bitfield.hasPiece(i)) {
        return false;
      }
    }

    return true;
  }

  // Gets the file size
  public long getFileSize() {
    return fileSize;
  }

  // Gets the standard piece size
  public int getStandardPieceSize() {
    return pieceSize;
  }

  // Closes file when done
  public void close() throws IOException {
    if (file != null) {
      file.close();
    }
  }
}
