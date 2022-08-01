export const AppSettings = {
  // List of settings.
  SETTING: {
    UI: {
      PIXABAY: {
        CATEGORY: "pixabay_category",
        KEY: "pixabay_key",
        ENABLED: "pixabay_enabled"
      }
    },
    SECURITY: {
      PLATFORM_CERTIFICATE: "platformCertificate"
    },
    DEVICE_REGISTRATION: {
      REGISTRATION_MODE: "deviceRegistrationMode",
      TAGS_ALGORITHM: "deviceTagsAlgorithm",
      ROOT_CA: "deviceRootCA"
    },
    PROVISIONING: {
      PROVISIONING_URL: "provisioningUrl",
    },
    GEOLOCATION: {
      LATITUDE: "geo_lat",
      LONGITUDE: "geo_lon",
    }
  },

  SETTING_VALUES: {
    UI: {
      PIXABAY: {
        CATEGORY: {
          ANIMALS: "animals",
          BACKGROUNDS: "backgrounds",
          BUILDINGS: "buildings",
          BUSINESS: "business",
          COMPUTER: "computer",
          EDUCATION: "education",
          FASHION: "fashion",
          FEELINGS: "feelings",
          FOOD: "food",
          HEALTH: "health",
          INDUSTRY: "industry",
          MUSIC: "music",
          NATURE: "nature",
          PEOPLE: "people",
          PLACES: "places",
          RELIGION: "religion",
          SCIENCE: "science",
          SPORTS: "sports",
          TRANSPORTATION: "transportation",
          TRAVEL: "travel"
        }
      }
    },
    DEVICE_REGISTRATION: {
      REGISTRATION_MODE: {
        DISABLED: "DISABLED",
        OPEN: "OPEN",
        OPEN_WITH_APPROVAL: "OPEN_WITH_APPROVAL",
        ID: "ID"
      },
      TAGS_ALGORITHM: {
        ALL: "ALL",
        ANY: "ANY"
      }
    },
    NETWORKING: {
      MQTT_ACL_ENDPOINT_STATUS: {
        ACTIVE: "ACTIVE",
        INACTIVE: "INACTIVE"
      }
    },
    PROVISIONING: {
      ENCRYPTION: {
        ENCRYPTED: "ENCRYPTED",
        NOT_ENCRYPTED: "NOT_ENCRYPTED"
      },
      SIGNATURE: {
        SIGNED: "SIGNED",
        NOT_SIGNED: "NOT_SIGNED",
      },
    }
  }
};

