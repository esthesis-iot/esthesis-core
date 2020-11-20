package esthesis.backend.config;

/**
 * Each internal class corresponds to a tab in the Settings UI
 */
public class AppSettings {

  private AppSettings() {
  }

  /* The name of the settings as held in the database. */
  public static class Setting {

    private Setting() {
    }

    public static class Security {

      private Security() {
      }

      public static final String OUTGOING_ENCRYPTION = "deviceOutgoingEncryption";
      public static final String INCOMING_ENCRYPTION = "deviceIncomingEncryption";
      public static final String OUTGOING_SIGNATURE = "deviceOutgoingSignature";
      public static final String INCOMING_SIGNATURE = "deviceIncomingSignature";
      public static final String PLATFORM_CERTIFICATE = "platformCertificate";
      public static final String AES_KEY = "aesKey";
    }
    public static class DeviceRegistration {

      private DeviceRegistration() {
      }

      public static final String REGISTRATION_MODE = "deviceRegistration";
      public static final String TAGS_ALGORITHM = "deviceTagsAlgorithm";
      public static final String ROOT_CA = "deviceRootCA";
    }

    public static class Provisioning {

      private Provisioning() {
      }

      public static final String URL = "provisioningUrl";
      public static final String AES_KEY = "provisioningAesKey";
      public static final String ENCRYPTION = "provisioningEncrypt";
      public static final String SIGNATURE = "provisioningSign";
    }
    public static class Geollocation {

      private Geollocation() {
      }

      public static final String LATITUDE = "geo_lat";
      public static final String LONGITUDE = "geo_lon";
    }
  }

  /* The possible values of each setting. */
  public static class SettingValues {

    private SettingValues() {
    }

    public static class Security {

      private Security() {
      }

      public static class OutgoingEncryption {

        private OutgoingEncryption() {
        }

        public static final String ENCRYPTED = "ENCRYPTED";
        public static final String NOT_ENCRYPTED = "NOT_ENCRYPTED";
      }
      public static class IncomingEncryption {

        private IncomingEncryption() {
        }

        public static final String ENCRYPTED = "ENCRYPTED";
        public static final String OPTIONAL = "OPTIONAL";
      }
      public static class OutgoingSignature {

        private OutgoingSignature() {
        }

        public static final String SIGNED = "SIGNED";
        public static final String NOT_SIGNED = "NOT_SIGNED";
      }
      public static class IncomingSignature {

        private IncomingSignature() {
        }

        public static final String SIGNED = "SIGNED";
        public static final String OPTIONAL = "OPTIONAL";
      }
    }
    public static class DeviceRegistration {

      private DeviceRegistration() {
      }

      public static class RegistrationMode {

        private RegistrationMode() {
        }

        public static final String DISABLED = "DISABLED";
        public static final String OPEN = "OPEN";
        public static final String OPEN_WITH_APPROVAL = "OPEN_WITH_APPROVAL";
        public static final String ID = "ID";
      }
      public static class TagsAlgorithm {

        private TagsAlgorithm() {
        }

        public static final String ALL = "ALL";
        public static final String ANY = "ANY";
      }
    }

    public static class Provisioning {

      private Provisioning() {
      }

      public static class Encryption {

        private Encryption() {
        }

        public static final String ENCRYPTED = "ENCRYPTED";
        public static final String NOT_ENCRYPTED = "NOT_ENCRYPTED";
      }
      public static class Signature {

        private Signature() {
        }

        public static final String SIGNED = "SIGNED";
        public static final String NOT_SIGNED = "NOT_SIGNED";
      }
    }
  }
}
