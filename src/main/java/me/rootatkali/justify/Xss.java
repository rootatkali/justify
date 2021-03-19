package me.rootatkali.justify;

public class Xss {
  public static String deXss(String input) {
    return input.replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#x27;");
  }
}
