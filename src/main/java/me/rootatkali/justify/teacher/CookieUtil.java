package me.rootatkali.justify.teacher;

import java.util.HashMap;
import java.util.Map;

public class CookieUtil {
  public static String convert(HashMap<String, String> cookies) {
    StringBuilder sb = new StringBuilder();
  
    for (Map.Entry<String, String> e : cookies.entrySet()) { // Loop for cookies from login
      sb.append(
          String.format("%s=%s; ", e.getKey(), e.getValue())
      );
    }
    String result = sb.toString().trim();
  
    if (result.length() > 0 && result.lastIndexOf(";") == result.length() - 1) {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }
  
  public static HashMap<String, String> convert(String cookie) {
    HashMap<String, String> result = new HashMap<>();
    
    String[] cookies = cookie.split("; ");
    for (String c : cookies) {
      String[] kv = c.split("=", 2);
      result.put(kv[0], kv[1]);
    }
    
    return result;
  }
}
