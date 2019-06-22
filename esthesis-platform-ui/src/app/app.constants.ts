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

  // Device settings.
  DEVICE: {
    STATE: {
      DISABLED: "DISABLED",
      PREREGISTERED: "PREREGISTERED",
      REGISTERED: "REGISTERED",
      APPROVAL: "APPROVAL"
    }
  },

  // User statuses.
  USER_STATUS: {
    DISABLED: 0,
    ENABLED: 1
  },

  // Available formatters for field values.
  FIELD_VALUE_FORMATTER: {
    DATE_SHORT: "DATE_SHORT",
    DATE_MEDIUM: "DATE_MEDIUM",
    DATE_LONG: "DATE_LONG",
    DATETIME_SHORT: "DATETIME_SHORT",
    DATETIME_MEDIUM: "DATETIME_MEDIUM",
    DATETIME_LONG: "DATETIME_LONG",
    DURATION_MSEC: "DURATION_MSEC",
    BYTES_TO_MB: "BYTES_TO_MB",
    BYTES_TO_GB: "BYTES_TO_GB",
    FAHRENHEIT_TO_CELCIUS: "FAHRENHEIT_TO_CELCIUS",
    CELCIUS_TO_FAHRENHEIT: "CELCIUS_TO_FAHRENHEIT",
  }
};

