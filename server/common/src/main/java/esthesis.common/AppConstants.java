package esthesis.common;

public class AppConstants {

  public static class Messaging {

    private Messaging() {

    }

    // The prefix to be used for all Kafka topics.
    public static final String CHANNEL_PREFIX = "esthesis-";
  }

  public static class Registry {

    private Registry() {
    }

    public static final String SECURITY_ASYMMETRIC_KEY_SIZE = "securityAsymmetricKeySize";
    public static final String SECURITY_ASYMMETRIC_KEY_ALGORITHM = "securityAsymmetricKeyAlgorithm";
    public static final String SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM = "securityAsymmetricSignatureAlgorithm";
    public static final String DEVICE_ROOT_CA = "deviceRootCA";
    public static final String PROVISIONING_URL = "provisioningURL";
  }

  public static class Device {

    private Device() {
    }

    public static class State {

      private State() {
      }

      public static final String DISABLED = "DISABLED";
      public static final String PREREGISTERED = "PREREGISTERED";
      public static final String REGISTERED = "REGISTERED";
      public static final String APPROVAL = "APPROVAL";
    }
  }
//  public static class Audit {
//
//    private Audit() {
//    }
//
//    public static class Event {
//
//      private Event() {
//      }
//
//      public static final String CA = "Certificate Authority";
//      public static final String CERTIFICATE = "Certificate";
//      public static final String AUTHENTICATION = "Authentication";
//      public static final String APPLICATION = "Application";
//      public static final String USER = "User";
//    }
//
//    public static class Level {
//
//      private Level() {
//      }
//
//      public static final String UPDATE = "Update";
//      public static final String DELETE = "Delete";
//      public static final String INFO = "Info";
//      public static final String SECURITY = "Security";
//      public static final String CREATE = "Create";
//    }
//  }

//  public static class Application {
//
//    private Application() {
//    }
//
//    public static class Status {
//
//      private Status() {
//      }
//
//      public static final String INACTIVE = "0";
//      public static final String ACTIVE = "1";
//    }
//  }

//  public static class User {
//
//    private User() {
//    }
//
//    // Status is, exceptionally, byte as it is fetched to qlack-fuse-aaa.
//    public class Status {
//
//      private Status() {
//      }
//
//      public static final byte INACTIVE = 0;
//      public static final byte ACTIVE = 1;
//      public static final byte APP_USER = 2;
//    }
//  }

//
//  public static class Cryptography {
//
//    private Cryptography() {
//    }
//
//    public static class KeyType {
//
//      private KeyType() {
//      }
//
//      public static final int PRIVATE_KEY = 0;
//      public static final int PUBLIC_KEY = 1;
//      public static final int CERTIFICATE = 2;
//    }
//
//    public static class Type {
//      private Type() {}
//
//      public static final int CA = 0;
//      public static final int CERTIFICATE = 1;
//    }
//  }
//
//  public static class Jwt {
//
//    private Jwt() {
//    }
//
//    // The unique JWT id.
//    public static final String CLAIM_EMAIL = "email";
//  }
//
//  public static class ExitCodes {
//
//    private ExitCodes() {
//    }
//
//    public static final int CANT_GENERATE_PLATFORM_AES_KEY = 1;
//    public static final int CANT_GENERATE_PROVISIONING_AES_KEY = 2;
//  }
//
//  @AllArgsConstructor
//  @Getter
//  public enum NIFI_SINK_HANDLER {
//
//    PING(1),
//    METADATA(2),
//    TELEMETRY(3),
//    SYSLOG(4),
//    FILESYSTEM(5),
//    COMMAND(6);
//
//    private final int type;
//    private static final Map<Integer, NIFI_SINK_HANDLER> map = new HashMap<>();
//
//    static {
//      for (NIFI_SINK_HANDLER nifi_sink_handler : NIFI_SINK_HANDLER.values()) {
//        map.put(nifi_sink_handler.type, nifi_sink_handler);
//      }
//    }
//
//    public static NIFI_SINK_HANDLER valueOf(int type) {
//      return map.get(type);
//    }
//  }
//
//  public static class DigitalTwins {
//    public enum Type {
//      telemetry, metadata, command
//    }
//    public enum DTOperations {
//      QUERY, MIN, MAX, COUNT, MEAN, SUM
//    }
//  }
//
//  public static class Dashboard {
//    public static class WidgetType {
//      // Keep these inline with the beans under esthesis.platform.backend.server.service.widgets.
//      public static final String SENSOR_VALUE = "sensorValue";
//    }
//  }
//
//  public static class NiFi {
//    public static final String SINKS_PACKAGE = "esthesis.platform.backend.server.nifi.sinks.";
//    public static class QueryResults {
//      public static final String TIMESTAMP = "timestamp";
//      public static final String TYPE = "type";
//    }
//  }
//
//  public static class Campaign {
//    public static class Member {
//      public static class Type {
//        public static final int DEVICE = 1;
//        public static final int TAG = 2;
//      }
//    }
//    public static class State {
//      public static final int CREATED = 10;
//      public static final int RUNNING = 20;
//      public static final int PAUSED_BY_USER = 30;
//      public static final int PAUSED_BY_WORKFLOW = 40;
//      public static final int TERMINATED_BY_WORKFLOW = 50;
//      public static final int TERMINATED_BY_USER = 60;
//    }
//    public static class Type {
//      public static final int PROVISIONING = 1;
//      public static final int COMMAND = 2;
//      public static final int REBOOT = 3;
//      public static final int SHUTDOWN = 4;
//    }
//    public static  class Condition {
//      public static class Type {
//        public static final int DATETIME = 1;
//        public static final int SUCCESS = 2;
//        public static final int PROPERTY = 4;
//        public static final int PAUSE = 5;
//        public static final int BATCH = 6;
//      }
//      public static  class Stage {
//        public static final int ENTRY = 1;
//        public static final int EXIT = 2;
//        public static String of(int stage) {
//          return switch (stage) {
//            case ENTRY -> "Entry";
//            case EXIT -> "Exit";
//            default -> "Global";
//          };
//        }
//      }
//      public static  class Op {
//        public static final int BEFORE = 1;
//        public static final int AFTER = 2;
//        public static final int ABOVE = 3;
//        public static final int BELOW = 4;
//        public static final int FOREVER = 5;
//        public static final int TIMER_MINUTES = 6;
//        public static final int EQUAL = 7;
//        public static final int GT = 8;
//        public static final int LT = 9;
//        public static final int GTE = 10;
//        public static final int LTE = 11;
//      }
//    }
//  }
}
