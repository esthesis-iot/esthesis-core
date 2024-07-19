package esthesis.dataflows.oriongateway.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  // The name of an attribute to hold the esthesis ID for this device.
  @WithDefault("esthesisId")
  String attributeEsthesisId();

  // The name of an attribute to hold the esthesis Hardware ID for this device.
  @WithDefault("esthesisHardwareId")
  String attributeEsthesisHardwareId();

  // The URL of the Orion Context Broker.
  String orionUrl();

  // Flag to indicate if new esthesis devices should be created in Orion.
  @WithDefault("true")
  boolean orionCreateDevice();

  // Flag to indicate if esthesis devices should be deleted from Orion.
  @WithDefault("true")
  boolean orionDeleteDevice();

  // Flag to indicate if esthesis telemetry/metadata should be updated in Orion
  @WithDefault("true")
  boolean orionUpdateData();

  // A device attribute indicating whether data for this device (i.e. device metrics) should be
  // updated in Orion or not. If this attribute is not specified, data is updated in Orion. If
  // this attribute is specified, a value of "true" indicates that the device should be registered,
  // any other value indicates that the device should not be registered.
  Optional<String> orionUpdateDataAttribute();

  @WithDefault("true")
  boolean orionUpdateGeolocation();

  // Indicates whether esthesis devices that were available prior to the Orion Gateway being
  // added should be registered in Orion or not. Orion Gateway dataflows checks for such devices
  // once every 24h.
  @WithDefault("true")
  boolean orionRetroCreateDevicesOnSchedule();

  @WithDefault("0 0 0 * * ?")
  String orionRetroCreateDevicesSchedule();

  // Add devices to Orion that were available prior to the Orion Gateway being added on boot.
  @WithDefault("true")
  boolean orionRetroCreateDevicesOnBoot();

  // The default type to use when registering devices, if orionTypeAttribute is not set.
  String orionDefaultType();

  // The name of the device attribute that contains the device type.
  Optional<String> orionTypeAttribute();

  // The name of the attribute that contains the device ID. If not present, the device ID (i.e.
  // the ID with which the device is registered in the platform) is used. if Defined, it must be a valid URL
  Optional<String> orionIdAttribute();

  // A prefix to add to the device ID when registering a new device, if
  // orionIdAttribute is not set.
	@WithDefault("urn:ngsi-ld:")
  String orionIdPrefix();

  // A device attribute indicating whether this device should be registered in Orion or not. If
  // this attribute is not specified, all devices are registered. If this attribute is specified,
  // a value of "true" indicates that the device should be registered, any other value
  // indicates that the device should not be registered.
  Optional<String> orionRegistrationEnabledAttribute();

  // The Kafka consumer group id.
  Optional<String> kafkaConsumerGroup();

  // The Kafka topic to consume telemetry messages from.
  Optional<String> kafkaTelemetryTopic();

  // The Kafka topic to consume metadata messages from.
  Optional<String> kafkaMetadataTopic();

  // The Kafka topic to consume application notifications from.
  Optional<String> kafkaApplicationTopic();

  // The URL of the Kafka cluster to connect to.
  String kafkaClusterUrl();

	// The security protocol to use when connecting to the Kafka cluster. If empty, no security
	// protocol will be setup.
	Optional<String> kafkaSecurityProtocol();

	// The SASL mechanism to use when connecting to the Kafka cluster. If empty, no SASL mechanism
	// will be setup.
	Optional<String> kafkaSaslMechanism();

	// The JAAS configuration to use when connecting to the Kafka cluster. If empty, no JAAS
	// configuration will be setup.
	Optional<String> kafkaJaasConfig();

	// The number of messages that can be queued for processing.
	int concurrencyQueueSize();

	// How often the queue is polled for new messages (in milliseconds).
	int concurrencyPollTimeout();

	// The maximum number of concurrent consumers.
	int concurrencyConsumers();

  // The name of the metadata attribute to indicate an attribute is maintained by esthesis.
  @WithDefault("maintainedBy")
  String esthesisOrionMetadataName();

  // The name of the metadata attribute to indicate what was the source of an attribute in
  // Orion maintained by esthesis (i.e. an esthesis device attribute, an esthesis telemetry value,
  // or an esthesis metadata value).
  @WithDefault("attributeSource")
  String esthesisAttributeSourceMetadataName();

  // The value of the metadata attribute to indicate an attribute is maintained by esthesis.
  @WithDefault("esthesis")
  String esthesisOrionMetadataValue();


	// Orion LD Contexts defined, separated by comma.
	@WithDefault("https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld")
	String orionLdDefinedContexts();

	// Orion LD Contexts Rel defined, separated by comma and respecting the defined contexts order
	@WithDefault("http://www.w3.org/ns/json-ld#context")
	String orionLdDefinedContextsRelationships();

	//Custom measurement structure using Qute formmater
	Optional<String> customMeasurementFormatterAttributeName();

	// A list off attributes name, separated by comma, to be synchronized and send to orion.
	// If defined, only the attributes containing the values in this list will be stored in Orion
	Optional<String>  orionAttributesToSync();
}
