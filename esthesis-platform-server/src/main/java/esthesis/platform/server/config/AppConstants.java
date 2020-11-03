package esthesis.platform.server.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Various constants used throughout the application.
 */
public class AppConstants {

  public class Audit {

    public class Event {

      public static final String CA = "Certificate Authority";
      public static final String CERTIFICATE = "Certificate";
      public static final String AUTHENTICATION = "Authentication";
      public static final String APPLICATION = "Application";
      public static final String USER = "User";
    }

    public class Level {

      public static final String UPDATE = "Update";
      public static final String DELETE = "Delete";
      public static final String INFO = "Info";
      public static final String SECURITY = "Security";
      public static final String CREATE = "Create";
    }
  }

  public class Application {

    public class Status {

      public static final String INACTIVE = "0";
      public static final String ACTIVE = "1";
    }
  }

  public class User {

    // Status is, exceptionally, byte as it is fetched to qlack-fuse-aaa.
    public class Status {

      public static final byte INACTIVE = 0;
      public static final byte ACTIVE = 1;
      public static final byte APP_USER = 2;
    }
  }

  public class Device {

    public class State {

      public static final String DISABLED = "DISABLED";
      public static final String PREREGISTERED = "PREREGISTERED";
      public static final String REGISTERED = "REGISTERED";
      public static final String APPROVAL = "APPROVAL";
    }
  }

  public class Cryptography {

    //TODO Turn them to String
    public class KeyType {

      public static final int PRIVATE_KEY = 0;
      public static final int PUBLIC_KEY = 1;
      public static final int CERTIFICATE = 2;
    }
  }

  public class WebSocket {

    public static final String TOPIC_PREFIX = "/topic";

    public class Topic {

      public final static String DEVICE_REGISTRATION = "deviceRegistration";
    }
  }

  public class Jwt {

    // The unique JWT id.
    public static final String CLAIM_EMAIL = "email";
  }

  public class ExitCodes {

    public static final int CANT_GENERATE_PLATFORM_AES_KEY = 1;
    public static final int CANT_GENERATE_PROVISIONING_AES_KEY = 2;
  }

  @AllArgsConstructor
  @Getter
  public enum NIFI_SINK_HANDLER {

    PING(1),
    METADATA(2),
    TELEMETRY(3),
    SYSLOG(4),
    FILESYSTEM(5);

    private final int type;
    private static Map map = new HashMap<>();

    static {
      for (NIFI_SINK_HANDLER nifi_sink_handler : NIFI_SINK_HANDLER.values()) {
        map.put(nifi_sink_handler.type, nifi_sink_handler);
      }
    }

    public static NIFI_SINK_HANDLER valueOf(int type) {
      return (NIFI_SINK_HANDLER) map.get(type);
    }

  }

  //  public class Http {
  //
  //    public static final String AUTHORIZATION = "Authorization";
  //  }

  public static class DigitalTwins {

    public static class Type {

      public static final String TELEMETRY = "telemetry";
      public static final String METADATA = "metadata";
      public static final String COMMAND = "command";
    }

    public static class DTOperations {

      public static final String OPERATION_QUERY = "QUERY";
      public static final String OPERATION_MIN = "MIN";
      public static final String OPERATION_MAX = "MAX";
      public static final String OPERATION_COUNT = "COUNT";
      public static final String OPERATION_MEAN = "MEAN";
      public static final String OPERATION_SUM = "SUM";
      public static final String[] SUPPORTED_OPERATIONS = {OPERATION_COUNT, OPERATION_MAX,
        OPERATION_MEAN,
        OPERATION_MIN, OPERATION_QUERY, OPERATION_SUM};
    }
  }

  public static class NiFiQueryResults {

    public static final String TIMESTAMP = "timestamp";
    public static final String TYPE = "type";
  }
}
