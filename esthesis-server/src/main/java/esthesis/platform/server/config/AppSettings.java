package esthesis.platform.server.config;

/**
 * Each internal class corresponds to a tab in the Settings UI
 */
public class AppSettings {

  private AppSettings() {
  }

  /* The name of the settings as held in the database. */
  public static class Setting {

    public static class UI {

      public static final String PIXABAY_KEY = "pixabay_key";
      public static final String PIXABAY_CATEGORY = "pixabay_category";
      public static final String PIXABAY_ENABLED = "pixabay_enabled";
    }

    public static class Security {

      public static final String PLATFORM_CERTIFICATE = "platformCertificate";
      public static final String AES_KEY = "aesKey";
    }

    public static class Device {
      public static final String LATITUDE = "geo_lat";
      public static final String LONGITUDE = "geo_lon";
    }

    public static class DeviceRegistration {

      public static final String REGISTRATION_MODE = "deviceRegistration";
      public static final String TAGS_ALGORITHM = "deviceTagsAlgorithm";
      public static final String ROOT_CA = "deviceRootCA";
    }

    public static class Provisioning {

      public static final String URL = "provisioningUrl";
      public static final String AES_KEY = "provisioningAesKey";
      public static final String ENCRYPTION = "provisioningEncrypt";
      public static final String SIGNATURE = "provisioningSign";
    }
  }

  /* The possible values of each setting. */
  public static class SettingValues {

    public static class DeviceRegistration {

      public static class RegistrationMode {

        public static final String DISABLED = "DISABLED";
        public static final String OPEN = "OPEN";
        public static final String OPEN_WITH_APPROVAL = "OPEN_WITH_APPROVAL";
        public static final String ID = "ID";
      }

      public static class TagsAlgorithm {

        public static final String ALL = "ALL";
        public static final String ANY = "ANY";
      }
    }
  }
}
