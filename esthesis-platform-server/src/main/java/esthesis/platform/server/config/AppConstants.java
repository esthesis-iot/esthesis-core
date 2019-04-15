package esthesis.platform.server.config;

/**
 * Various constants used throughout the application.
 */
public class AppConstants {

  public class Audit {
    public static final String EVENT_CA = "CA management";
    public static final String EVENT_CERTS = "Certificates";
    public final static String EVENT_AUTHENTICATION = "Authentication";
    public final static String EVENT_APPLICATION = "Application";
    public final static String EVENT_PROFILE = "Profile";
  }

  //TODO Turn them to String
  public class Application {
    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_DISABLED = -1;
  }

  //TODO Turn them to String
  public class User {
    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_DISABLED = -1;

    public static final int SYSTEM_USER_ID = 0;
  }

  public class Device {
    public class Status {
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
    public static final int CONNECT_RETRY = 29;
  }

  public class WebSocket {
    public static final String TOPIC_PREFIX = "/topic";
    public class Topic {
      public final static String DEVICE_REGISTRATION = "deviceRegistration";
    }
  }

  public class Event {
    public static final String MQTT_CONFIGURATION_EVENT_PATH = "/esthesis/platform/configuration/mqtt";
    public static final String ZOOKEEPER_CONFIGURATION_EVENT_PATH = "/esthesis/platform/configuration/zookeeper";
    public static final String DATA_SINK_CONFIGURATION_EVENT_PATH = "/esthesis/platform/configuration/datasink";
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
}