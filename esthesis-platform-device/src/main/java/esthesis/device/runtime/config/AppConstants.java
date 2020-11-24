package esthesis.device.runtime.config;

public class AppConstants {

  private AppConstants() {
  }

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

    private ExitCode() {
    }

    public static final int CANNOT_FIND_DEVICE_ID = 1;
  }

  // The types of forking when a provisioning package is downloaded.
  public static final String PROVISIONING_FORK_TYPE_SOFT = "soft";
  public static final String PROVISIONING_FORK_TYPE_HARD = "hard";

    public static class Mqtt {

      private Mqtt() {
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
