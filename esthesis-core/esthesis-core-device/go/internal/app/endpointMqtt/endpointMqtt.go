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

		if topic == telemetryEndpoint {
			// Check if payload should be transformed.
			if config.Flags.LuaMqttTelemetryScript != "" {
				payload = []byte(luaExecutor.ExecuteLuaScript(string(payload[:]),
					config.Flags.LuaMqttTelemetryScript))
			}
			mqttClient.Publish(config.Flags.
				TopicTelemetry+"/"+config.Flags.HardwareId,
				payload).WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)
		} else if topic == metadataEndpoint {
			// Check if payload should be transformed.
			if config.Flags.LuaMqttMetadataScript != "" {
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
