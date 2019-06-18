package esthesis.platform.server.config;

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

  public class Zookeeper {

    public static final String LEADER_ELECTION_PATH_GLOBAL = "/esthesis/platform/leader/global";
    public static final String LEADER_ELECTION_PATH_MQTT = "/esthesis/platform/leader/mqtt";
    public static final String MQTT_CONFIGURATION_EVENT_PATH = "/esthesis/platform/configuration/mqtt";
    public static final String ZOOKEEPER_CONFIGURATION_EVENT_PATH = "/esthesis/platform/configuration/zookeeper";
    public static final String DATA_SINK_CONFIGURATION_EVENT_PATH = "/esthesis/platform/configuration/datasink";
    public static final int CONNECT_RETRY = 29;
  }

  public class WebSocket {

    public static final String TOPIC_PREFIX = "/topic";

    public class Topic {

      public final static String DEVICE_REGISTRATION = "deviceRegistration";
    }
  }

  public class Jwt {

    // The unique JWT id.
    public static final String JWT_CLAIM_ID = "jti";
    // The time in seconds ago this JWT was generated.
    public static final String JWT_CLAIM_CREATED_AT = "iat";
    public static final String JWT_CLAIM_EMAIL = "sub";
    public static final String JWT_CLAIM_ISSUER = "iss";
    public static final String JWT_CLAIM_EXPIRES_AT = "exp";
    public static final String JWT_CLAIM_USER_ID = "user_id";
    public static final String JWT_CLAIM_SESSION_ID = "session_id";
  }

  public class Infrastructure {

    //TODO Turn them to String
    public class MqttServerType {

      public static final long UNMANAGED = 0;
      public static final long MANAGED_MOSQUITTO = 1;
    }
  }

  public class Virtualization {

    //TODO Turn them to String
    public class Type {

      public static final int DOCKER_ENGINE = 0;
      public static final int DOCKER_SWARM = 1;
    }

    //TODO Turn them to String
    public class Security {

      public static final int OPEN = 0;
      public static final int CERTIFICATE = 1;
    }

    public class Container {

      public class RestartPolicy {

        public static final String NONE = "NONE";
        public static final String ON_FAILURE = "ON_FAILURE";
        public static final String ALWAYS = "ALWAYS";
        public static final String UNLESS_STOPPED = "UNLESS_STOPPED";
        public static final String ANY = "ANY";
      }
    }
  }

  public class ExitCodes {

    public static final int CANT_GENERATE_PLATFORM_AES_KEY = 1;
    public static final int CANT_GENERATE_PROVISIONING_AES_KEY = 2;
  }
}
