package esthesis.util.kafka.notifications.common;

public class KafkaNotificationsConstants {

	/*
	The names of the SmallRye channels application components use to communicate with each other.

  Note that these are the names of the SmallRye Kafka channels, not the name of the underlying
  Kafka topics. The actual Kafka topic those two channels point to is defined in the
  application YAML file. We prefer to keep this configuration in the application YAML instead;
  although it is possible to dynamically set the topic name of an outgoing channel, it is
  not possible to do so for an incoming channel using the provided annotations.

  Also note, although Quarkus needs those two channel names to be different, the underlying
  the should both point in application YAML to the same Kafka topic.
   */
	public static final String SMALLRYE_KAFKA_CHANNEL_IN = "esthesis-app-in";
	public static final String SMALLRYE_KAFKA_CHANNEL_OUT = "esthesis-app-out";

	public enum Component {
		UNSPECIFIED,
		DEVICE, TAG, CERTIFICATE, CA
	}

	public enum Subject {
		UNSPECIFIED,
		DEVICE, DEVICE_ATTRIBUTE, TAG, CERTIFICATE, CA
	}

	public enum Action {
		UNSPECIFIED,
		CREATE,
		UPDATE,
		CREATEORUPDATE,
		DELETE,
	}

}
