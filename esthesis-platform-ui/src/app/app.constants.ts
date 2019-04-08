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
      USERNAME: 'sub'
    }
  },

  // The different types of security keys and CERTIFICATE.
  KEY_TYPE: {
    PRIVATE_KEY: 0,
    PUBLIC_KEY: 1,
    CERTIFICATE: 2
  },

  // List of settings.
  SETTING: {
    DEVICE_REGISTRATION: {
      _KEY: 'deviceRegistration',
      _VAL: {
        DISABLED: 'DISABLED',
        OPEN: 'OPEN',
        ID: 'ID',
        CRYPTO: 'CRYPTO',
        OPEN_WITH_APPROVAL: 'OPEN_WITH_APPROVAL'
      }
    },
    DEVICE_PUSH_TAGS: {
      _KEY: 'devicePushTags'
    },
    // How devices should transmit data.
    DEVICE_DATA_ENCRYPTION_MODE: {
      _KEY: 'deviceDataEncryptionMode',
      _VAL: {
        ENCRYPTION_NOT_SUPPORTED: 0,
        ENCRYPTION_OPTIONAL: 1,
        ENCRYPTION_REQUIRED: 2
      }
    },
    MQTT: {
      SUPERUSER_CERTIFICATE: {
        _KEY: 'mqttSuperuserCertificate'
      },
      ACL_ENDPOINT_STATUS: {
        _KEY: 'mqttAclEndpointStatus',
        _VAL: {
          INACTIVE: 0,
          ACTIVE: 1
        }
      }
    }
  },

  // WebSocket topics to subscribe.
  WEBSOCKET: {
    TOPIC_PREFIX: '/topic',
    TOPIC: {
      DEVICE_REGISTRATION: '/deviceRegistration'
    }
  },

  // Available types of Virtualization supported.
  VIRTUALIZATION: {
    TYPE: {
      DOCKER_ENGINE: 0,
      DOCKER_SWARM: 1
    },
    SECURITY: {
      OPEN: 0,
      CERTIFICATE: 1
    }
  },

  // Applicaton status.
  APPLICATION_STATE: {
    ACTIVE: 1,
    DISABLED: -1,
    INACTIVE: 0
  },

  // Generic lookups.
  GENERIC: {
    ACTIVE: 1,
    DISABLED: 0
  }
};

