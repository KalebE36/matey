package edu.ufl.cnt4007.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CommonConfig {
    private final int numberOfPreferredNeighbors;
    private final int unchokingInterval;
    private final int optimisticUnchokingInterval;
    private final String fileName;
    private final long fileSize;
    private final int pieceSize;

    private void validateFileSize() throws IOException {
        long actualSize = Files.size(Paths.get(this.fileName));
        if (actualSize != this.fileSize) {
            throw new IOException(this.fileName + " size mismatch. Expected: " + this.fileSize + ", actual: " + actualSize);
        }
    }

    public CommonConfig() throws IOException {
        // Parse Common.cfg
        Map<String, String> cfg = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("Common.cfg"))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] parts = line.split("\\s+", 2);
                if (parts.length == 2)
                    cfg.put(parts[0], parts[1]);
            }
        }

        this.numberOfPreferredNeighbors = Integer.parseInt(cfg.get("NumberOfPreferredNeighbors"));
        this.unchokingInterval = Integer.parseInt(cfg.get("UnchokingInterval"));
        this.optimisticUnchokingInterval = Integer.parseInt(cfg.get("OptimisticUnchokingInterval"));
        this.fileName = cfg.get("FileName");
        this.fileSize = Long.parseLong(cfg.get("FileSize"));
        this.pieceSize = Integer.parseInt(cfg.get("PieceSize"));

        validateFileSize();
    }

    public int getNumberOfPreferredNeighbors() {
        return numberOfPreferredNeighbors;
    }

    public int getUnchokingInterval() {
        return unchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }

    @Override
    public String toString() {
        return "CommonConfig{" +
                "numberOfPreferredNeighbors=" + numberOfPreferredNeighbors +
                ", unchokingInterval=" + unchokingInterval +
                ", optimisticUnchokingInterval=" + optimisticUnchokingInterval +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", pieceSize=" + pieceSize +
                '}';
    }
}
