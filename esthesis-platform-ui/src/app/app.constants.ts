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
};

