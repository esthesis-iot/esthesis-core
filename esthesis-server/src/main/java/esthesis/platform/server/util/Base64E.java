package esthesis.platform.server.util;

import org.apache.commons.codec.binary.Base64;

/**
 * A proxy to Base64 encoding/decoding functionality to easily switch implementations.
 */
public class Base64E {

  private Base64E() {
  }

  public static String encode(byte[] payload) {
    return Base64.encodeBase64String(payload);
  }

  public static byte[] decode(String payload) {
    return Base64.decodeBase64(payload);
  }

  public static byte[] decode(byte[] payload) {
    return Base64.decodeBase64(payload);
  }
}
