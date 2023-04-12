package esthesis.util.kafka.notifications.common;

public class KafkaNotificationsConstants {

  // The name of the SmallRye channels application components use to communicate with each other.
  //
  // This is the name of the SmallRye Kafka channels, not the name of the Kafka topics. The
  // actual Kafka topic those two channels point to is defined in the application.properties
  // file. We'd prefer to have this configuration in the application settings instead, however
  // although it is possible to dynamically set the topic name of an outgoing channel, it is
  // not possible to do so for an incoming channel using the provided annotations.
  public static final String SMALLRYE_KAFKA_CHANNEL_IN = "esthesis-app-in";
  public static final String SMALLRYE_KAFKA_CHANNEL_OUT = "esthesis-app-out";

  public enum Component {
    UNSPECIFIED,
    DEVICE, TAG, CERTIFICATE
  }

  public enum Subject {
    UNSPECIFIED,
    DEVICE, DEVICE_ATTRIBUTE, TAG, CERTIFICATE
  }

  public enum Action {
    UNSPECIFIED,
    CREATE,
    DELETE,
    UPDATE
  }

}
