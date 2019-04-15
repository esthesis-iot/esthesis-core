export const AppSettings = {
  // List of settings.
  SETTING: {
    SECURITY: {
      OUTGOING_ENCRYPTION: "deviceOutgoingEncryption",
      INCOMING_ENCRYPTION: "deviceIncomingEncryption",
      OUTGOING_SIGNATURE: "deviceOutgoingSignature",
      INCOMING_SIGNATURE: "deviceIncomingSignature",
      PLATFORM_CERTIFICATE: "platformCertificate"
    },
    DEVICE_REGISTRATION: {
      REGISTRATION_MODE: "deviceRegistration",
      PUSH_TAGS: "devicePushTags",
      IGNORE_DURING_DEVICE_REGISTRATION: "ignoreDuringDeviceRegistration"
    },
    NETWORKING: {
      MQTT_ACL_ENDPOINT_STATUS: "mqttAclEndpointStatus"
    }
  },

  SETTING_VALUES: {
    SECURITY: {
      OUTGOING_ENCRYPTION: {
        ENCRYPTED: "ENCRYPTED",
        NOT_ENCRYPTED: "NOT_ENCRYPTED",
        DEVICE_SPECIFIC: "DEVICE_SPECIFIC"
      },
      INCOMING_ENCRYPTION: {
        ENCRYPTED: "ENCRYPTED",
        NOT_ENCRYPTED: "NOT_ENCRYPTED",
        OPTIONAL: "OPTIONAL"
      },
      OUTGOING_SIGNATURE: {
        SIGNED: "SIGNED",
        NOT_SIGNED: "NOT_SIGNED",
        DEVICE_SPECIFIC: "DEVICE_SPECIFIC"
      },
      INCOMING_SIGNATURE: {
        SIGNED: "SIGNED",
        NOT_SIGNED: "NOT_SIGNED",
        OPTIONAL: "OPTIONAL"
      }
    },
    DEVICE_REGISTRATION: {
      REGISTRATION_MODE: {
        DISABLED: "DISABLED",
        OPEN: "OPEN",
        OPEN_WITH_APPROVAL: "OPEN_WITH_APPROVAL",
        ID: "ID",
        CRYPTO: "CRYPTO"
      },
      IGNORE_DURING_DEVICE_REGISTRATION: {
        ENCRYPTION: "ENC",
        SIGNATURE: "SIG",
        ENCRYPTION_AND_SIGNATURE: "SIG_ENC"
      },
      PUSH_TAGS: {
        ALLOWED: "ALLOWED",
        IGNORED: "IGNORED"
      }
    },
    NETWORKING: {
      MQTT_ACL_ENDPOINT_STATUS: {
        ACTIVE: "ACTIVE",
        INACTIVE: "INACTIVE"
      }
    }
  }
};

