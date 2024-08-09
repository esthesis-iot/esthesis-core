export const AppConstants = {
  // The API root context.
  API_ROOT: "/api",

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
    DEVICE_PROVISIONING_SEMVER: "DEVICE_PROVISIONING_SEMVER",
    DEVICE_REGISTRATION_MODE: "DEVICE_REGISTRATION_MODE",
    DEVICE_REGISTRATION_SECRET: "DEVICE_REGISTRATION_SECRET",
    DEVICE_ROOT_CA: "DEVICE_ROOT_CA",
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
    },
    DATA_IMPORT: {
      TYPE: {
        TELEMETRY: "TELEMETRY",
        METADATA: "METADATA"
      },
      SOURCE: {
        TEXT: "TEXT",
        FILE: "FILE"
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
      INTERNAL: "INTERNAL",
      EXTERNAL: "EXTERNAL"
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

  SECURITY: {
    // The name of session keys where user data is saved.
    SESSION_STORAGE: {
      USERDATA: "X-ESTHESIS-USERDATA",
      PERMISSIONS: "X-ESTHESIS-PERMISSIONS"
    },
    // The name of fields in user data.
    USERDATA: {
      USERNAME: "preferred_username",
      FIRST_NAME: "given_name",
      LAST_NAME: "family_name"
    },
    // ERN
    ERN: {
      ROOT: "ern",
      SYSTEM: "esthesis",
      SUBSYSTEM: "core"
    },
    // Equivalent to AppConstants.Security.Category
    CATEGORY: {
      ABOUT: "about",
      APPLICATION: "application",
      AUDIT: "audit",
      CA: "ca",
      CAMPAIGN: "campaign",
      CERTIFICATES: "certificates",
      COMMAND: "command",
      CRYPTO: "crypto",
      DASHBOARD: "dashboard",
      DATAFLOW: "dataflow",
      DEVICE: "device",
      INFRASTRUCTURE: "infrastructure",
      KEYSTORE: "keystore",
      KUBERNETES: "kubernetes",
      PROVISIONING: "provisioning",
      SECURITY: "security",
      SETTINGS: "settings",
      TAGS: "tags",
      USERS: "users",
      GROUPS: "groups",
      ROLES: "roles",
      POLICIES: "policies"
    },
    // Equivalent to AppConstants.Security.Operation
    OPERATION: {
      CREATE: "create",
      READ: "read",
      WRITE: "write",
      DELETE: "delete"
    },
    PERMISSION: {
      ALLOW: "allow",
      DENY: "deny"
    }
  }

};

