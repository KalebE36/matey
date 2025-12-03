package edu.ufl.cnt4007.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.ufl.cnt4007.protocol.Bitfield;

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

    // Initialize file size
    initializeFile(fileName);
  }

  /**
   * Initializes the file by either writing empty data, or copying
   * the file you want to seed from ROOT directory.
   * 
   * @throws IOException
   */
  private void initializeFile(String fileName) throws IOException {
    if (bitfield == null) {
      throw new IOException("Bitfield is not initialized");
    }

    File rootFile = new File(fileName); // Assuming ROOT is current working dir

    if (rootFile.exists() && rootFile.isFile()) {
      // Copy the full file from ROOT to peer directory
      try (RandomAccessFile src = new RandomAccessFile(rootFile, "r")) {
        byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer
        long remaining = src.length();
        long offset = 0;

        while (remaining > 0) {
          int bytesRead = src.read(buffer, 0, (int) Math.min(buffer.length, remaining));
          file.seek(offset);
          file.write(buffer, 0, bytesRead);
          offset += bytesRead;
          remaining -= bytesRead;
        }
      }
    } else {
      // No ROOT file, fill missing pieces with zeros
      for (int i = 0; i < totalPieces; i++) {
        if (!bitfield.hasPiece(i)) {
          long offset = (long) i * pieceSize;
          int currentSize = getPieceSize(i);
          byte[] emptyData = new byte[currentSize];
          file.seek(offset);
          file.write(emptyData);
        }
      }
    }

    file.getFD().sync(); // Ensure all data is flushed
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
