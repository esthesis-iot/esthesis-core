package esthesis.platform.server.config;

/**
 * Each internal class corresponds to a tab in the Settings UI
 */
public class AppSettings {

  /* The name of the settings as held in the database. */
  public class Setting {
    public class Security {
      public static final String OUTGOING_ENCRYPTION = "deviceOutgoingEncryption";
      public static final String INCOMING_ENCRYPTION = "deviceIncomingEncryption";
      public static final String OUTGOING_SIGNATURE = "deviceOutgoingSignature";
      public static final String INCOMING_SIGNATURE = "deviceIncomingSignature";
      public static final String PLATFORM_CERTIFICATE = "platformCertificate";
      public static final String AES_KEY = "aesKey";
    }
    public class DeviceRegistration {
      public static final String REGISTRATION_MODE = "deviceRegistration";
      public static final String PUSH_TAGS = "devicePushTags";
    }
    public class Networking {
      public static final String MQTT_ACL_ENDPOINT_STATUS = "mqttAclEndpointStatus";
    }
    public class Provisioning {
      public static final String URL = "provisioningUrl";
      public static final String AES_KEY = "provisioningAesKey";
      public static final String ENCRYPTION = "provisioningEncrypt";
      public static final String SIGNATURE = "provisioningSign";
    }
    public class Geollocation {
      public static final String LATITUDE = "geo_lat";
      public static final String LONGITUDE = "geo_lon";
    }
  }

  /* The possible values of each setting. */
  public class SettingValues {
    public class Security {
      public class OutgoingEncryption {
        public static final String ENCRYPTED = "ENCRYPTED";
        public static final String NOT_ENCRYPTED = "NOT_ENCRYPTED";
      }
      public class IncomingEncryption {
        public static final String ENCRYPTED = "ENCRYPTED";
        public static final String OPTIONAL = "OPTIONAL";
      }
      public class OutgoingSignature {
        public static final String SIGNED = "SIGNED";
        public static final String NOT_SIGNED = "NOT_SIGNED";
      }
      public class IncomingSignature {
        public static final String SIGNED = "SIGNED";
        public static final String OPTIONAL = "OPTIONAL";
      }
    }
    public class DeviceRegistration {
      public class RegistrationMode {
        public static final String DISABLED = "DISABLED";
        public static final String OPEN = "OPEN";
        public static final String OPEN_WITH_APPROVAL = "OPEN_WITH_APPROVAL";
        public static final String ID = "ID";
      }
      public class PushTags {
        public static final String ALLOWED = "ALLOWED";
        public static final String IGNORED = "IGNORED";
      }
    }

    public class Networking {
      public class MqttAclEndpointStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String INACTIVE = "INACTIVE";
      }
    }

    public class Provisioning {
      public class ENCRYPTION {
        public static final String ENCRYPTED = "ENCRYPTED";
        public static final String NOT_ENCRYPTED = "NOT_ENCRYPTED";
      }
      public class SIGNATURE {
        public static final String SIGNED = "SIGNED";
        public static final String NOT_SIGNED = "NOT_SIGNED";
      }
    }
  }

}
