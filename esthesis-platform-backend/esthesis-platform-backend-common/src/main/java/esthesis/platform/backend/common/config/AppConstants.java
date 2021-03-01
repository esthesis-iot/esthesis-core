package esthesis.platform.backend.common.config;

public class AppConstants {

  private AppConstants() {
  }

  public static class Generic {
    // A generic 'System' persona to be used when no user-specific information needs to be handled.
    public static final String SYSTEM = "System";
  }

  public static class Device {
    public static class CommandReply {
      public static final String PAYLOAD_ENCODING_PLAIN = "plain";
      public static final String PAYLOAD_ENCODING_BASE64 = "base64";
    }
    public static class CommandType {
      public static final String PROVISIONING = "PROVISIONING";
      public static final String PING = "PING";
      public static final String HEALTH = "HEALTH";
      public static final String REBOOT = "REBOOT";
      public static final String EXECUTE = "EXECUTE";
    }
  }


}
