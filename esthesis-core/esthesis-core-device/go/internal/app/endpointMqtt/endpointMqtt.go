package endpointMqtt

import (
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/luaExecutor"
	mqtt "github.com/mochi-co/mqtt/server"
	"github.com/mochi-co/mqtt/server/events"
	"github.com/mochi-co/mqtt/server/listeners"
	log "github.com/sirupsen/logrus"
	"strconv"
	"time"
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
	log.Errorf("No custom LUA handler found for custom telemetry topic '%s'.", topicName)
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
	log.Errorf("No custom LUA handler found for custom metadata topic '%s'.", topicName)
	return ""
}

func Start(done chan bool) {
	mqttListeningAddress := config.Flags.
		EndpointMqttListeningIP + ":" + strconv.Itoa(config.Flags.EndpointMqttListeningPort)

	// Create the  MQTT Server.
	server := mqtt.NewServer(nil)

	// Create a TCP listener.
	tcp := listeners.NewTCP("t1", mqttListeningAddress)

	// Add the listener to the server with default options (nil).
	err := server.AddListener(tcp, nil)
	if err != nil {
		log.Fatal(err)
	}

	// Specify message handlers.
	server.Events.OnMessage = func(cl events.Client, pk events.Packet) (pkx events.Packet,
		err error) {
		topic := pk.TopicName
		payload := pk.Payload

		// Process the incoming message according to the topic it was sent to.
		if topic == telemetryEndpoint || isCustomTelemetryTopic(topic) {
			// Check if payload should be transformed.
			if isCustomTelemetryTopic(topic) && getCustomTelemetryTopicLuaHandler(topic) != "" {
				payload = []byte(luaExecutor.ExecuteLuaScript(string(payload[:]),
					getCustomTelemetryTopicLuaHandler(topic)))
			} else if !isCustomTelemetryTopic(topic) && config.Flags.LuaMqttTelemetryScript != "" {
				payload = []byte(luaExecutor.ExecuteLuaScript(string(payload[:]),
					config.Flags.LuaMqttTelemetryScript))
			}
			mqttClient.Publish(config.Flags.
				TopicTelemetry+"/"+config.Flags.HardwareId,
				payload).WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)
		} else if topic == metadataEndpoint || isCustomMetadataTopic(topic) {
			// Check if payload should be transformed.
			if isCustomMetadataTopic(topic) && getCustomMetadataTopicLuaHandler(topic) != "" {
				payload = []byte(luaExecutor.ExecuteLuaScript(string(payload[:]),
					getCustomMetadataTopicLuaHandler(topic)))
			} else if !isCustomMetadataTopic(topic) && config.Flags.LuaMqttMetadataScript != "" {
				payload = []byte(luaExecutor.ExecuteLuaScript(string(payload[:]),
					config.Flags.LuaMqttMetadataScript))
			}
			mqttClient.Publish(config.Flags.
				TopicMetadata+"/"+config.Flags.HardwareId,
				payload).WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)
		} else {
			log.Errorf("Received a messages on an unknown topic '%s'.", topic)
		}

		return pk, nil
	}

	go func() {
		log.Infof("Starting embedded MQTT server started at '%s'.",
			mqttListeningAddress)
		err := server.Serve()
		if err != nil {
			log.Fatal(err)
		}
	}()

	<-done
	log.Debug("Stopping embedded MQTT server.")
	server.Close()
}
