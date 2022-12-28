package esthesis.common;

public class AppConstants {

  // The topic prefix to use when a Kafka topic needs to be created.
  //TODO to be removed with Settings preferences
  public static final String KAFKA_TOPIC_PREFIX = "esthesis-";

  // Certain DFL implementations need to be looked up in various places in
  // esthesis. For example, to be able to send a Control Request to a device,
  // you need to have the MQTT Client DFL configured. Since DFLs are defined
  // dynamically in Angular, we need to have a way to identify them in the
  // backend too. Not all DFLs need to be identified, but the ones that do
  // are next.
  public static final String DFL_MQTT_CLIENT_NAME = "mqtt-client";

  // Redis key suffixes for measurements.
  public static final String REDIS_KEY_SUFFIX_VALUE_TYPE = "valueType";
  public static final String REDIS_KEY_SUFFIX_TIMESTAMP = "timestamp";
  public static final String REDIS_KEY_PROVISIONING_PACKAGE_FILE = "file";

  // Settings keys.
  public enum NamedSetting {
    SECURITY_ASYMMETRIC_KEY_SIZE, SECURITY_ASYMMETRIC_KEY_ALGORITHM,
    SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM,

    PLATFORM_CERTIFICATE,

    DEVICE_ROOT_CA, DEVICE_PROVISIONING_URL, DEVICE_REGISTRATION_MODE,
    DEVICE_TAGS_ALGORITHM, DEVICE_GEO_LAT, DEVICE_GEO_LON,
    DEVICE_PROVISIONING_SECURE, DEVICE_PROVISIONING_CACHE_TIME,

    KAFKA_TOPIC_COMMAND_REQUEST, KAFKA_TOPIC_EVENT_TAG_DELETE
  }
  
  // The status a device can have.
  public enum DeviceStatus {
    DISABLED, PREREGISTERED, REGISTERED, APPROVAL;
  }

  // The available registration modes of the platform.
  public enum DeviceRegistrationMode {
    DISABLED, OPEN, OPEN_WITH_APPROVAL, ID
  }

  public static class Provisioning {

    public enum ConfigOption {
      FTP_HOST, FTP_PORT, FTP_USERNAME, FTP_PASSWORD, FTP_PATH, FTP_PASSIVE,
      WEB_URL, WEB_USERNAME, WEB_PASSWORD,
      MINIO_URL, MINIO_BUCKET, MINIO_OBJECT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY,
      S3_OBJECT, S3_BUCKET
    }

    public enum Type {
      ESTHESIS, WEB, FTP, MINIO
    }

    public enum CacheStatus {
      NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
    }

    public static class Redis {

      private Redis() {
      }

      public static final String DOWNLOAD_TOKEN_PACKAGE_ID = "DTPI";
      public static final String DOWNLOAD_TOKEN_CREATED_ON = "DTCO";
    }
  }

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
