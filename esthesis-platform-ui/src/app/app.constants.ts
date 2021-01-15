export const AppConstants = {
  // The name of the JWT key in local storage.
  JWT_STORAGE_NAME: 'esthesis_platform',

  // The date format to use.
  DATE_FORMAT: 'yyyy-MM-dd HH:mm:ss',

  // The root URL of the API.
  API_ROOT: '/api',

  // The claims available in JWT.
  jwt: {
    claims: {
      EMAIL: 'email'
    }
  },

  // The different types of security keys and CERTIFICATE.
  KEY_TYPE: {
    PRIVATE_KEY: 0,
    PUBLIC_KEY: 1,
    CERTIFICATE: 2
  },

  // Device settings.
  DEVICE: {
    STATE: {
      DISABLED: 'DISABLED',
      PREREGISTERED: 'PREREGISTERED',
      REGISTERED: 'REGISTERED',
      APPROVAL: 'APPROVAL'
    }
  },

  // User statuses.
  USER_STATUS: {
    DISABLED: 0,
    ENABLED: 1
  },

  // Available formatters for field values.
  FIELD_VALUE_FORMATTER: {
    DATE_SHORT: 'DATE_SHORT',
    DATE_MEDIUM: 'DATE_MEDIUM',
    DATE_LONG: 'DATE_LONG',
    DATETIME_SHORT: 'DATETIME_SHORT',
    DATETIME_MEDIUM: 'DATETIME_MEDIUM',
    DATETIME_LONG: 'DATETIME_LONG',
    DURATION_MSEC: 'DURATION_MSEC',
    BYTES_TO_MB: 'BYTES_TO_MB',
    BYTES_TO_GB: 'BYTES_TO_GB',
    FAHRENHEIT_TO_CELCIUS: 'FAHRENHEIT_TO_CELCIUS',
    CELCIUS_TO_FAHRENHEIT: 'CELCIUS_TO_FAHRENHEIT'
  },

  // Measurement types.
  MEASUREMENT_TYPE: {
    TELEMETRY: 'TELEMETRY',
    METADATA: 'METADATA'
  },

  // The different types of NiFiSinks handlers.
  HANDLER: {
    PING: 1,
    METADATA: 2,
    TELEMETRY: 3,
    SYSLOG: 4,
    FILESYSTEM: 5,
    COMMAND: 6
  },

  // Campaign constants.
  CAMPAIGN: {
    TYPE: {
      PROVISIONING: 0,
      COMMAND: 1,
      REBOOT: 2,
      SHUTDOWN: 3
    },
    CONSTRAINT: {
      TYPE: {
        DATETIME: 0,
        SUCCESS: 1,
        FAILURE: 2,
        PROPERTY: 3,
        PAUSE: 4,
        BATCH: 5
      },
      STAGE: {
        ENTRY: 0,
        EXIT: 1,
        CONTINUOUS: 2
      },
      CONDITION_TYPE: {
        BEFORE: 0,
        AFTER: 1,
        ABOVE: 2,
        BELOW: 3,
        FOREVER: 4,
        TIMER_MINUTES: 5,
        EQUAL: 6,
        GT: 7,
        LT: 8,
        GTE: 9,
        LTE: 10
      }
    }
  },


};

