package esthesis.common.config;

public class AppConstants {

  // HTTP header delivering DT security token.
  public static final String XESTHESISDT_HEADER = "X-ESTHESIS-DT";

  public static class Generic {

    // A generic 'System' persona to be used when no user-specific information needs to be handled.
    public static final String SYSTEM = "System";
  }


  // The list of commands that can be sent to devices.
  public enum MqttCommand {
    // Check for latest provisioning packages.
    PROVISIONING_CHECK_NEW,
    // Ping the platform.
    PING,
    // Send health data.
    HEALTH,
    // Reboot the device.
    REBOOT,
    // Arbitrary command (as defined on payload).
    EXECUTE
  }
}
