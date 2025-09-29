package endpointMqtt

import (
	"strconv"
	"time"

	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/buffer"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/luaExecutor"
	mqtt "github.com/mochi-co/mqtt/server"
	"github.com/mochi-co/mqtt/server/events"
	"github.com/mochi-co/mqtt/server/listeners"
	log "github.com/sirupsen/logrus"
)

const telemetryEndpoint = "telemetry"
const metadataEndpoint = "metadata"

// Check if the provided topic name is one of the custom telemetry topics.
func isCustomTelemetryTopic(topicName string) bool {
	for i := 0; i < len(config.Flags.LuaExtraMqttTelemetryTopic); i += 2 {
		if topicName == config.Flags.LuaExtraMqttTelemetryTopic[i] {
			return true
		}
	}
	return false
}

// Find the LUA handler for the provided custom telemetry topic name.
func getCustomTelemetryTopicLuaHandler(topicName string) string {
	for i := 0; i < len(config.Flags.LuaExtraMqttTelemetryTopic); i += 2 {
		if topicName == config.Flags.LuaExtraMqttTelemetryTopic[i] {
			return config.Flags.LuaExtraMqttTelemetryTopic[i+1]
		}
	}
	return ""
}

// Check if the provided topic name is one of the custom metadata topics.
func isCustomMetadataTopic(topicName string) bool {
	for i := 0; i < len(config.Flags.LuaExtraMqttMetadataTopic); i += 2 {
		if topicName == config.Flags.LuaExtraMqttMetadataTopic[i] {
			return true
		}
	}
	return false
}

// Find the LUA handler for the provided custom metadata topic name.
func getCustomMetadataTopicLuaHandler(topicName string) string {
	for i := 0; i < len(config.Flags.LuaExtraMqttMetadataTopic); i += 2 {
		if topicName == config.Flags.LuaExtraMqttMetadataTopic[i] {
			return config.Flags.LuaExtraMqttMetadataTopic[i+1]
		}
	}
	return ""
}

func Start(done chan bool, buff buffer.Buffer) {
	mqttListeningAddress := config.Flags.
		EndpointMqttListeningIP + ":" + strconv.Itoa(config.Flags.EndpointMqttListeningPort)

	inflightTTLDuration := config.Flags.MqttInflightTTLDuration

	// Create Server configurable options
	opts := &mqtt.Options{
		InflightTTL: int64(inflightTTLDuration),
	}

	// Create the  MQTT Server.
	server := mqtt.NewServer(opts)

	// Create a TCP listener.
	tcp := listeners.NewTCP("t1", mqttListeningAddress)

	// Create authentication handler
	authHandler := createAuth(config.Flags.EndpointMqttAuthUsername, config.Flags.EndpointMqttAuthPassword)

	// Add the listener to the server with the created auth handler
	err := server.AddListener(tcp, &listeners.Config{Auth: authHandler})
	if err != nil {
		log.Fatal(err)
	}

	// Specify message handlers.
	server.Events.OnMessage = func(cl events.Client, pk events.Packet) (pkx events.Packet,
		err error) {
		topic := pk.TopicName
		payload := pk.Payload
		log.Debugf("Received a message on topic '%s' with payload '%s'.", topic, payload)

		// Process the incoming message according to the topic it was sent to.
		if topic == telemetryEndpoint || isCustomTelemetryTopic(topic) {
			// Check if payload should be transformed.
			if isCustomTelemetryTopic(topic) && getCustomTelemetryTopicLuaHandler(topic) != "" {
				payload = []byte(luaExecutor.ExecuteLuaScript(topic, string(payload[:]),
					getCustomTelemetryTopicLuaHandler(topic)))
			} else if !isCustomTelemetryTopic(topic) && config.Flags.LuaMqttTelemetryScript != "" {
				payload = []byte(luaExecutor.ExecuteLuaScript(topic, string(payload[:]),
					config.Flags.LuaMqttTelemetryScript))
			}
			topic := config.Flags.TopicTelemetry + "/" + config.Flags.HardwareId
			published := mqttClient.Publish(topic, payload)

			// If publishing fails, store the message in the buffer to retry later.
			if !published {
				item := buffer.Message{Timestamp: time.Now().UnixNano(), Payload: payload, Topic: topic}
				buff.Store(item)
			}

		} else if topic == metadataEndpoint || isCustomMetadataTopic(topic) {
			// Check if payload should be transformed.
			if isCustomMetadataTopic(topic) && getCustomMetadataTopicLuaHandler(topic) != "" {
				payload = []byte(luaExecutor.ExecuteLuaScript(topic, string(payload[:]),
					getCustomMetadataTopicLuaHandler(topic)))
			} else if !isCustomMetadataTopic(topic) && config.Flags.LuaMqttMetadataScript != "" {
				payload = []byte(luaExecutor.ExecuteLuaScript(topic, string(payload[:]),
					config.Flags.LuaMqttMetadataScript))
			}

			topic := config.Flags.TopicMetadata + "/" + config.Flags.HardwareId
			published := mqttClient.Publish(topic, payload)

			// If failed to publish, store the message in the buffer to try sending again
			if !published {
				item := buffer.Message{Timestamp: time.Now().UnixNano(), Payload: payload, Topic: topic}
				buff.Store(item)
			}

		} else {
			log.Errorf("Received a messages on an unknown topic '%s'.", topic)
		}

		return pk, nil
	}

	go func() {
		log.Infof("Starting embedded MQTT server at '%s'.",
			mqttListeningAddress)
		err := server.Serve()
		if err != nil {
			log.Fatal(err)
		}
	}()

	<-done
	log.Debug("Stopping embedded MQTT server.")
	err = server.Close()
	if err != nil {
		log.Fatal(err)
	}
}
