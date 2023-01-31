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
    DEVICE_PUSHED_TAGS,

    KAFKA_TOPIC_COMMAND_REQUEST, KAFKA_TOPIC_EVENT_TAG_DELETE
  }

  public enum KeyType {
    PUBLIC, PRIVATE, CERTIFICATE
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

  public static class Audit {

    public enum Category {
      ABOUT,
      APPLICATION,
      AUDIT,
      CAMPAIGN,
      COMMAND,
      CRYPTO,
      DATAFLOW,
      DEVICE,
      DIGITAL_TWIN,
      INFRASTRUCTURE,
      KUBERNETES,
      PROVISIONING,
      SETTINGS,
      TAG,
      OTHER,
      NULL
    }

    public enum Operation {
      READ, WRITE, DELETE, OTHER, NULL
    }
  }

  public static class Campaign {

    public static class Member {

      public enum Type {
        DEVICE, TAG;
      }
    }

    public enum State {

      CREATED, RUNNING, PAUSED_BY_USER, PAUSED_BY_WORKFLOW,
      TERMINATED_BY_WORKFLOW, TERMINATED_BY_USER;
    }

    public enum Type {

      PROVISIONING, EXECUTE_COMMAND, REBOOT, SHUTDOWN;
    }

    public static class Condition {

      public enum Type {
        DATETIME, SUCCESS, PROPERTY, PAUSE, BATCH;
      }

      public enum Stage {
        ENTRY, EXIT, INSIDE;
      }

      public enum Op {

        BEFORE, AFTER, ABOVE, BELOW, FOREVER, TIMER_MINUTES, EQUAL,
        GT, LT, GTE, LTE;
      }
    }
  }
}
