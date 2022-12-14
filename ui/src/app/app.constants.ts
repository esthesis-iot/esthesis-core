export const AppConstants = {
  // An id designating a new record is to be created.
  NEW_RECORD_ID: "new",

  // The date format to use.
  DATE_FORMAT: "yyyy-MM-dd HH:mm:ss",

  // The different types of security keys and CERTIFICATE.
  KEY_TYPE: {
    PRIVATE_KEY: 0,
    PUBLIC_KEY: 1,
    CERTIFICATE: 2
  },

  // A list of application-wide settings (see also the counterpart Java class AppConstants.java).
  NAMED_SETTING: {
    DEVICE_PROVISIONING_URL: "DEVICE_PROVISIONING_URL",
    DEVICE_PROVISIONING_SECURE: "DEVICE_PROVISIONING_SECURE",
    DEVICE_REGISTRATION_MODE: "DEVICE_REGISTRATION_MODE",
    DEVICE_TAGS_ALGORITHM: "DEVICE_TAGS_ALGORITHM",
    DEVICE_ROOT_CA: "DEVICE_ROOT_CA",
    PLATFORM_CERTIFICATE: "PLATFORM_CERTIFICATE",
    KAFKA_TOPIC_COMMAND_REQUEST: "KAFKA_TOPIC_COMMAND_REQUEST",
    DEVICE_PROVISIONING_CACHE_TIME: "DEVICE_PROVISIONING_CACHE_TIME"
  },

  // Device settings.
  DEVICE: {
    STATUS: {
      DISABLED: "DISABLED",
      PREREGISTERED: "PREREGISTERED",
      REGISTERED: "REGISTERED",
      APPROVAL: "APPROVAL"
    },

    SETTING: {
      DEVICE_GEO_LAT: "DEVICE_GEO_LAT",
      DEVICE_GEO_LON: "DEVICE_GEO_LON"
    },

    COMMAND: {
      TYPE: {
        EXECUTE: "e",
        HEALTH: "h",
        PING: "p",
        FIRMWARE: "f",
        REBOOT: "r",
        SHUTDOWN: "s"
      },
      EXECUTION: {
        SYNCHRONOUS: "s",
        ASYNCHRONOUS: "a"
      }
    }
  },

  // User statuses.
  USER_STATUS: {
    DISABLED: 0,
    ENABLED: 1
  },

  // Provisioning.
  PROVISIONING: {
    TYPE: {
      ESTHESIS: "ESTHESIS",
      WEB: "WEB",
      FTP: "FTP",
      MINIO: "MINIO"
    },
    CONFIG: {
      FTP: {
        HOST: "HOST",
        PORT: "PORT",
        USERNAME: "USERNAME",
        PASSWORD: "PASSWORD",
        PATH: "PATH",
        PASSIVE: "PASSIVE"
      }
    },
    CACHE_STATUS: {
      NOT_STARTED: "NOT_STARTED",
      IN_PROGRESS: "IN_PROGRESS",
      COMPLETED: "COMPLETED",
      FAILED: "FAILED"
    }
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

  DIALOG_RESULT: {
    CANCEL: 0,
    SAVE: 1,
    DELETE: 2,
    OK: 3
  },

};

