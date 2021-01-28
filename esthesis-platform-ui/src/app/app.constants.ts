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
    STATE: {
      CREATED: 1,
      STARTED: 2,
      PAUSED: 3,
      TERMINATED: 4
    },
    TYPE: {
      PROVISIONING: 1,
      COMMAND: 2,
      REBOOT: 3,
      SHUTDOWN: 4
    },
    CONDITION: {
      TYPE: {
        DATETIME: 1,
        SUCCESS: 2,
        FAILURE: 3,
        PROPERTY: 4,
        PAUSE: 5,
        BATCH: 6
      },
      STAGE: {
        ENTRY: 1,
        EXIT: 2
      },
      OP: {
        BEFORE: 1,
        AFTER: 2,
        ABOVE: 3,
        BELOW: 4,
        FOREVER: 5,
        TIMER_MINUTES: 6,
        EQUAL: 7,
        GT: 8,
        LT: 9,
        GTE: 10,
        LTE: 11
      }
    },
    MEMBER_TYPE: {
      DEVICE: 1,
      TAG: 2
    }
  },


};

