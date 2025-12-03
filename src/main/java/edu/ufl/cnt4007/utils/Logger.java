package edu.ufl.cnt4007.utils;

public class Logger {
  public static void info(String message) {
    System.out.println("[INFO] " + message);
  }

  public static void error(String message) {
    System.err.println("[ERROR] " + message);
  }

  public static void debug(String message) {
    System.out.println("[DEBUG] " + message);
  }

  public static void warn(String message) {
    System.out.println("[WARN] " + message);
  }
}
