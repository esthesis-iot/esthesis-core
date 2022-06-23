export const AppConstants = {
  // An id designating a new record is to be created.
  NEW_RECORD_ID: 'new',
  
  // The date format to use.
  DATE_FORMAT: 'yyyy-MM-dd HH:mm:ss',

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
    },
    COMMANDS: {
      EXECUTE: 'EXECUTE',
      HEALTH: 'HEALTH',
      PING: 'PING',
      PROVISIONING: 'PROVISIONING',
      REBOOT: 'REBOOT',
      SHUTDOWN: 'SHUTDOWN'
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
      CREATED: 10,
      RUNNING: 20,
      PAUSED_BY_USER: 30,
      PAUSED_BY_WORKFLOW: 40,
      TERMINATED_BY_WORKFLOW: 50,
      TERMINATED_BY_USER: 60,
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

  DASHBOARD: {
    WIDGETS: {
      SENSOR_VALUE: "sensorValue",
      SENSOR_GAUGE: "sensorGauge",
      MAP: "map"
    }
  },

  DIALOG_RESULT: {
    CANCEL: 0,
    SAVE: 1,
    DELETE: 2,
    OK: 3
  }
};

