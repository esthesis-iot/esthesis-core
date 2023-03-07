export const AppConstants = {
  // An id designating a new record is to be created.
  NEW_RECORD_ID: "new",

  // The date format to use.
  DATE_FORMAT: "yyyy-MM-dd HH:mm:ss",

  // The different types of security keys and CERTIFICATE.
  KEY_TYPE: {
    PRIVATE_KEY: "PRIVATE",
    PUBLIC_KEY: "PUBLIC",
    CERTIFICATE: "CERTIFICATE"
  },

  // A list of application-wide settings (see also the counterpart Java class AppConstants.java).
  NAMED_SETTING: {
    DEVICE_PROVISIONING_URL: "DEVICE_PROVISIONING_URL",
    DEVICE_PROVISIONING_SECURE: "DEVICE_PROVISIONING_SECURE",
    DEVICE_REGISTRATION_MODE: "DEVICE_REGISTRATION_MODE",
    DEVICE_REGISTRATION_SECRET: "DEVICE_REGISTRATION_SECRET",
    DEVICE_ROOT_CA: "DEVICE_ROOT_CA",
    PLATFORM_CERTIFICATE: "PLATFORM_CERTIFICATE",
    KAFKA_TOPIC_COMMAND_REQUEST: "KAFKA_TOPIC_COMMAND_REQUEST",
    DEVICE_PROVISIONING_CACHE_TIME: "DEVICE_PROVISIONING_CACHE_TIME",
    DEVICE_PUSHED_TAGS: "DEVICE_PUSHED_TAGS"
  },

  // Keystore items.
  KEYSTORE: {
    ITEM: {
      RESOURCE_TYPE: {
        DEVICE: "DEVICE",
        CERTIFICATE: "CERT",
        CA: "CA",
        TAG: "TAG"
      },
      KEY_TYPE: {
        PRIVATE_KEY: "PRIVATE",
        CERTIFICATE: "CERT"
      }
    }
  },

  // Device settings.
  DEVICE: {
    ATTRIBUTE: {
      TYPE: {
        STRING: "STRING",
        BOOLEAN: "BOOLEAN",
        BYTE: "BYTE",
        SHORT: "SHORT",
        INTEGER: "INTEGER",
        LONG: "LONG",
        BIG_DECIMAL: "BIG_DECIMAL",
        UNKNOWN: "UNKNOWN"
      }
    },
    TYPE: {
      ESTHESIS: "ESTHESIS"
    },
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
      CREATED: "CREATED",
      RUNNING: "RUNNING",
      PAUSED_BY_USER: "PAUSED_BY_USER",
      PAUSED_BY_WORKFLOW: "PAUSED_BY_WORKFLOW",
      TERMINATED_BY_WORKFLOW: "TERMINATED_BY_WORKFLOW",
      TERMINATED_BY_USER: "TERMINATED_BY_USER",
    },
    TYPE: {
      PROVISIONING: "PROVISIONING",
      EXECUTE_COMMAND: "EXECUTE_COMMAND",
      REBOOT: "REBOOT",
      SHUTDOWN: "SHUTDOWN",
      PING: "PING",
    },
    CONDITION: {
      TYPE: {
        DATETIME: "DATETIME",
        SUCCESS: "SUCCESS",
        PROPERTY: "PROPERTY",
        PAUSE: "PAUSE",
        BATCH: "BATCH",
      },
      STAGE: {
        ENTRY: "ENTRY",
        EXIT: "EXIT",
        INSIDE: "INSIDE"
      },
      OP: {
        BEFORE: "BEFORE",
        AFTER: "AFTER",
        FOREVER: "FOREVER",
        TIMER_MINUTES: "TIMER_MINUTES",
        EQUAL: "EQUAL",
        GT: "GT",
        LT: "LT",
        GTE: "GTE",
        LTE: "LTE",
      }
    },
    MEMBER_TYPE: {
      DEVICE: "DEVICE",
      TAG: "TAG",
    }
  },

  DIALOG_RESULT: {
    CANCEL: 0,
    SAVE: 1,
    DELETE: 2,
    OK: 3
  },

};

