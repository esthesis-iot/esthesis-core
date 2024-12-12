package esthesis.util.kafka.notifications.common;

public class KafkaNotificationsConstants {

	/*
	The names of the SmallRye channels application components use to communicate with each other.

  Note that these are the names of the SmallRye Kafka channels, not the name of the underlying
  Kafka topics. The actual Kafka topic those two channels point to is defined in the
  application YAML file.

  Note that, although Quarkus needs those two channel names to be different, the underlying Kafka
  topic names should identical.
  */
	public static final String SMALLRYE_KAFKA_UNICAST_CHANNEL_IN = "esthesis-app-unicast-in";
	public static final String SMALLRYE_KAFKA_UNICAST_CHANNEL_OUT = "esthesis-app-unicast-out";
	public static final String SMALLRYE_KAFKA_BROADCAST_CHANNEL_IN = "esthesis-app-broadcast-in";
	public static final String SMALLRYE_KAFKA_BROADCAST_CHANNEL_OUT = "esthesis-app-broadcast-out";

	public enum Component {
		UNSPECIFIED,
		DEVICE, TAG, CERTIFICATE, CA, DASHBOARD
	}

	public enum Subject {
		UNSPECIFIED,
		DEVICE, DEVICE_ATTRIBUTE, TAG, CERTIFICATE, CA, DASHBOARD
	}

	public enum Action {
		UNSPECIFIED,
		CREATE,
		UPDATE,
		CREATEORUPDATE,
		DELETE,
		SUB, UNSUB, REFRESHSUB
	}

}
