package esthesis.common;

public class AppConstants {

	// The topic prefix to use when a Kafka topic needs to be created.
	//TODO to be removed with Settings preferences - is it used?
	public static final String KAFKA_TOPIC_PREFIX = "esthesis-";

	// A keyword used to indicate a new record being created.
	//TODO is it used?
	public static final String NEW_RECORD_ID = "new";

	// Redis key suffixes for measurements.
	public static final String REDIS_KEY_SUFFIX_VALUE_TYPE = "valueType";
	public static final String REDIS_KEY_SUFFIX_TIMESTAMP = "timestamp";
	public static final String REDIS_KEY_PROVISIONING_PACKAGE_FILE = "file";

	// Naming convention for hardware ids.
	public static final String HARDWARE_ID_REGEX = "^[a-zA-Z0-9_-]*$";

	// The size limit when displaying possibly large content in the logs.
	public static final int MESSAGE_LOG_ABBREVIATION_LENGTH = 4096;

	// The header containing the shared secret for device registration (when the platform operates
	// in that mode).
	//TODO where/how is this used?
	public static final String REGISTRATION_SECRET_HEADER_NAME = "X-ESTHESIS-REGISTRATION-SECRET";

	// Security roles.
	public static final String ROLE_USER = "user";
	public static final String ROLE_SYSTEM = "system";

	// Settings keys.
	public enum NamedSetting {
		SECURITY_ASYMMETRIC_KEY_SIZE, SECURITY_ASYMMETRIC_KEY_ALGORITHM,
		SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM,

		DEVICE_ROOT_CA, DEVICE_PROVISIONING_URL, DEVICE_REGISTRATION_MODE,
		DEVICE_REGISTRATION_SECRET,
		DEVICE_TAGS_ALGORITHM, DEVICE_GEO_LAT, DEVICE_GEO_LON,
		DEVICE_PROVISIONING_SECURE, DEVICE_PROVISIONING_CACHE_TIME,
		DEVICE_PUSHED_TAGS,

		KAFKA_TOPIC_COMMAND_REQUEST, KAFKA_TOPIC_TELEMETRY, KAFKA_TOPIC_METADATA,
		KAFKA_TOPIC_APPLICATION,
		// Application events
		//TODO we need a different naming convention, not starting with KAFKA_TOPIC
		KAFKA_TOPIC_EVENT_TAG_DELETE
	}

	public enum KeyType {
		PUBLIC, PRIVATE, CERTIFICATE
	}

	public enum DeviceRegistrationMode {
		DISABLED, OPEN, OPEN_WITH_SECRET, ID
	}

	public static class Device {

		public enum Type {
			ESTHESIS, OTHER
		}

		public enum Status {
			DISABLED, PREREGISTERED, REGISTERED
		}

		public static class Attribute {

			public enum Type {
				STRING, BOOLEAN, BYTE, SHORT, INTEGER, LONG, BIG_DECIMAL, UNKNOWN
			}
		}

	}

	public static class Keystore {

		public static class Item {

			public enum ResourceType {
				DEVICE, CERT, CA, TAG
			}

			public enum KeyType {
				PRIVATE, CERT
			}
		}
	}

	public static class Provisioning {

		public enum ConfigOption {
			FTP_HOST, FTP_PORT, FTP_USERNAME, FTP_PASSWORD, FTP_PATH, FTP_PASSIVE,
			WEB_URL, WEB_USERNAME, WEB_PASSWORD,
			MINIO_URL, MINIO_BUCKET, MINIO_OBJECT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY,
			S3_OBJECT, S3_BUCKET
		}

		public enum Type {
			ESTHESIS, WEB, FTP, MINIO
		}

		public enum CacheStatus {
			NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
		}

		public static class Redis {

			public static final String DOWNLOAD_TOKEN_PACKAGE_ID = "DTPI";
			public static final String DOWNLOAD_TOKEN_CREATED_ON = "DTCO";

			private Redis() {
			}
		}
	}

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
			SETTINGS,
			TAG,
			USERS,
			GROUPS,
			ROLES,
			POLICIES,
			NULL
		}

		public enum Operation {
			CREATE, READ, WRITE, DELETE, OTHER
		}

		public enum Permission {

			ALLOW, DENY;
		}

		public static class Ern {

			public static final String ROOT = "ern";
			public static final String SYSTEM = "esthesis";
			public static final String SUBSYSTEM = "core";
		}
	}

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

}
