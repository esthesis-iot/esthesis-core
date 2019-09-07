package esthesis.common.config;

public class AppConstants {

  // HTTP header delivering DT security token.
  public static final String XESTHESISDT_HEADER = "X-ESTHESIS-DT";

  public class Generic {

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

  public class Mqtt {

    // The names of JSON nodes expected into an MQTT message to be processed by the platform.
    public class MqttPayload {

      // The name of JSON node providing the name of the metric.
      public static final String METRIC_KEYNAME = "m";
      // The name of JSON node providing the value of the metric.
      public static final String VALUES_KEYNAME = "v";
      // The name of JSON node providing the timestamp of the metric. A timestamp is expected as an
      // EPOCH time in msec.
      public static final String TIMESTAMP_KEYNAME = "t";
    }

    // The default MQTT event types.
    public class EventType {
      public static final String TELEMETRY = "telemetry";
      public static final String CONTROL = "control";
      public static final String METADATA = "metadata";
      public static final String CONTROL_REQUEST = CONTROL + "/request";
      public static final String CONTROL_REPLY = CONTROL + "/reply";
      public static final String PING = "ping";
    }
  }
}
