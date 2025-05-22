package esthesis.core.common;

/**
 * Global application constants across all CORE modules.
 */
public class AppConstants {

	// A keyword used to indicate a new record being created.
	public static final String NEW_RECORD_ID = "new";

	// Redis key suffixes for measurements.
	public static final String REDIS_KEY_SUFFIX_VALUE_TYPE = "valueType";
	public static final String REDIS_KEY_SUFFIX_TIMESTAMP = "timestamp";
	public static final String REDIS_KEY_PROVISIONING_PACKAGE_FILE = "file";

	// Naming convention for hardware ids.
	public static final String HARDWARE_ID_REGEX = "^[a-zA-Z0-9_-]*$";

	// The size limit when displaying possibly large content in the logs.
	public static final int MESSAGE_LOG_ABBREVIATION_LENGTH = 4096;

	// Security roles.
	public static final String ROLE_USER = "user";
	public static final String ROLE_SYSTEM = "system";

	// Settings keys.
	public enum NamedSetting {
		SECURITY_ASYMMETRIC_KEY_SIZE, SECURITY_ASYMMETRIC_KEY_ALGORITHM,
		SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM,

		DEVICE_ROOT_CA, DEVICE_PROVISIONING_URL, DEVICE_REGISTRATION_MODE,
		DEVICE_REGISTRATION_SECRET, DEVICE_PROVISIONING_SEMVER,
		DEVICE_TAGS_ALGORITHM, DEVICE_GEO_LAT, DEVICE_GEO_LON,
		DEVICE_PROVISIONING_SECURE, DEVICE_PROVISIONING_CACHE_TIME,
		DEVICE_PUSHED_TAGS,

		KAFKA_TOPIC_COMMAND_REQUEST, KAFKA_TOPIC_TELEMETRY, KAFKA_TOPIC_METADATA,
		KAFKA_TOPIC_APPLICATION,

		CHATBOT_ENABLED
	}

	// Types of security keys.
	public enum KeyType {
		PUBLIC, PRIVATE, CERTIFICATE
	}

	// The registration mode available in CORE.
	public enum DeviceRegistrationMode {
		DISABLED, OPEN, OPEN_WITH_SECRET, ID
	}

	// Device-related constants.
	public static class Device {

		public enum Status {
			DISABLED, PREREGISTERED, REGISTERED
		}

		public enum DataImportType {
			TELEMETRY, METADATA
		}

		public static class Attribute {

			public enum Type {
				STRING, BOOLEAN, BYTE, SHORT, INTEGER, LONG, BIG_DECIMAL, UNKNOWN
			}
		}

	}

	// MongoDB GridFS constants.
	public static class GridFS {

		private GridFS() {
		}

		public static final String PROVISIONING_BUCKET_NAME = "ProvisioningPackageBucket";
		public static final String PROVISIONING_METADATA_NAME = "provisioningPackageId";
	}

	public static class Keystore {

		private Keystore() {
		}

		public static class Item {

			public enum ResourceType {
				DEVICE, CERT, CA, TAG
			}

			public enum KeyType {
				PRIVATE, CERT
			}
		}
	}

	// Constants to identify provisioning packages and cache them in Redis.
	public static class Provisioning {

		public enum Type {
			INTERNAL, EXTERNAL
		}

		public static class Redis {

			public static final String DOWNLOAD_TOKEN_PACKAGE_ID = "DTPI";
			public static final String DOWNLOAD_TOKEN_CREATED_ON = "DTCO";

			private Redis() {
			}
		}
	}

	// Security and Audit constants.
	public static class Security {

		public enum Category {
			ABOUT,
			APPLICATION,
			AUDIT,
			CA,
			CAMPAIGN,
			CERTIFICATES,
			COMMAND,
			CRYPTO,
			DASHBOARD,
			DATAFLOW,
			DEVICE,
			INFRASTRUCTURE,
			KEYSTORE,
			KUBERNETES,
			PROVISIONING,
			SECURITY,
			SETTINGS,
			TAGS,
			USERS,
			GROUPS,
			ROLES,
			POLICIES,
			NULL  // Required for annotations.
		}

		// Available operations.
		public enum Operation {
			CREATE, READ, WRITE, DELETE, AUDIT,
			NULL  // Required for annotations.
		}

		// Available permissions.
		public enum Permission {

			ALLOW, DENY;
		}

		// ERN common prefixes.
		public static class Ern {

			private Ern() {
			}

			public static final String ROOT = "ern";
			public static final String SYSTEM = "esthesis";
			public static final String SUBSYSTEM = "core";
		}
	}

	// Constants for the Campaigns functionality.
	public static class Campaign {

		public enum State {

			CREATED, RUNNING, PAUSED_BY_USER, PAUSED_BY_WORKFLOW,
			TERMINATED_BY_WORKFLOW, TERMINATED_BY_USER;
		}

		public enum Type {

			PROVISIONING, EXECUTE_COMMAND, REBOOT, SHUTDOWN, PING;
		}

		public static class Member {

			public enum Type {
				DEVICE, TAG;
			}
		}

		public static class Condition {

			public enum Type {
				DATETIME, SUCCESS, PROPERTY, PAUSE, BATCH;
			}

			public enum Stage {
				ENTRY, EXIT, INSIDE;
			}

			public enum Op {

				BEFORE, AFTER, ABOVE, BELOW, FOREVER, TIMER_MINUTES, EQUAL,
				GT, LT, GTE, LTE;
			}
		}
	}

	// Constants for the Dashboard functionality.
	public static class Dashboard {

		public enum Type {
			ABOUT, AUDIT, CAMPAIGNS, CHART, DATETIME, DEVICE_MAP, DEVICES_LAST_SEEN, DEVICES_LATEST,
			DEVICES_STATUS, DIFF, IMAGE, NOTES, SECURITY_STATS, SENSOR, SENSOR_ICON, TITLE
		}
	}
}
