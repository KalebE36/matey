package edu.ufl.cnt4007.core;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class PeerLogger {
  private static Logger logger;
  private static FileHandler fileHandler;
  private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static void init(int peerId) throws IOException {
    // Filename format: log_peer_[peerID].log
    String fileName = "log_peer_" + peerId + ".log";

    logger = Logger.getLogger("PeerLogger" + peerId);
    logger.setUseParentHandlers(false); // Disable console output for the logger itself if desired

    fileHandler = new FileHandler(fileName, true);
    fileHandler.setFormatter(
        new Formatter() {
          @Override
          public String format(LogRecord record) {
            // Format: [Time]: [Message]
            return String.format(
                "[%s]: %s%n", LocalDateTime.now().format(dtf), record.getMessage());
          }
        });

    logger.addHandler(fileHandler);
  }

  public static void log(String message) {
    if (logger != null) {
      logger.info(message);
    } else {
      System.out.println("Logger not initialized: " + message);
    }
  }

  public static void close() {
    if (fileHandler != null) fileHandler.close();
  }
}
