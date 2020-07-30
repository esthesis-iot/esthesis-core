package esthesis.device.runtime.config;

public class AppConstants {

  // The context root of the API of the PS.
  public static final String URL_PS_PREFIX = "/api";

  // The URL of the registration service in PS.
  public static final String URL_PS_REGISTER = URL_PS_PREFIX + "/agent/register";

  // The URL of the provisioning service in PS.
  public static final String URL_PS_PROVISIONING = URL_PS_PREFIX + "/agent/provisioning";
  public static final String URL_PS_PROVISIONING_INFO = URL_PS_PROVISIONING + "/info";
  public static final String URL_PS_PROVISIONING_DOWNLOAD = URL_PS_PROVISIONING + "/download";

  // Exit codes.
  public static class ExitCode {

    public static final int CANNOT_FIND_DEVICE_ID = 1;
  }

  // The types of forking when a provisioning package is downloaded.
  public static final String PROVISIONING_FORK_TYPE_SOFT = "soft";
  public static final String PROVISIONING_FORK_TYPE_HARD = "hard";

    public static class Mqtt {

      // The names of JSON nodes expected into an MQTT message to be processed by the platform.
      public static class MqttPayload {

        // The name of JSON node providing the name of the metric.
        public static final String METRIC_KEYNAME = "m";
        // The name of JSON node providing the value of the metric.
        public static final String VALUES_KEYNAME = "v";
        // The name of JSON node providing the timestamp of the metric. A timestamp is expected as an
        // EPOCH time in msec.
        public static final String TIMESTAMP_KEYNAME = "t";
      }

      // The default MQTT event types.
      public enum EventType {
        TELEMETRY,
        METADATA,
        CONTROL_REQUEST,
        CONTROL_REPLY,
        PING
      }
    }
}
