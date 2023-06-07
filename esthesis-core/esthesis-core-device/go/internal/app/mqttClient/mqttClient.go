package mqttClient

import (
	"fmt"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttCommandRequestReceiver"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/util"
	log "github.com/sirupsen/logrus"
	"os"
	"time"
)

var client mqtt.Client

func Disconnect() {
	if client != nil {
		log.Debug("Unsubscribing from Command Request topic.")
		commandRequestTopic := config.Flags.
			TopicCommandRequest + "/" + config.Flags.HardwareId
		if token := client.Unsubscribe(commandRequestTopic); token.Wait() && token.
			Error() != nil {
			fmt.Println(token.Error())
			os.Exit(1)
		}

		log.Debug("Disconnecting from MQTT server.")
		client.Disconnect(250)
	}
}

func Connect() bool {
	mqttServer := config.GetRegistrationProperty(config.RegistrationPropertyMqttServer)
	// If no MQTT server is configured, do nothing.
	if mqttServer == "" {
		log.Warn("No MQTT server configured.")
		return false
	}

	log.Debugf("Connecting to MQTT server '%s'.", mqttServer)
	opts := mqtt.NewClientOptions()
	opts.AddBroker(mqttServer)
	opts.SetClientID(config.Flags.HardwareId)
	opts.SetKeepAlive(60 * time.Second)

	client = mqtt.NewClient(opts)
	token := client.Connect()
	if token.Wait() && token.Error() != nil {
		panic(token.Error())
	}

	// Subscribe to command request topic.
	commandRequestTopic := config.Flags.
		TopicCommandRequest + "/" + config.Flags.HardwareId
	log.Debugf("Subscribing to topic '%s'.", commandRequestTopic)
	if token := client.Subscribe(commandRequestTopic, 0,
		mqttCommandRequestReceiver.OnMessage); token.Wait() && token.Error() != nil {
		fmt.Println(token.Error())
		os.Exit(1)
	}

	return true
}

func Publish(topic string, payload []byte) mqtt.Token {
	if client != nil {
		log.Debugf("Publishing '%s' to topic '%s'.", util.AbbrBA(payload), topic)
		return client.Publish(topic, 0, false, payload)
	} else {
		return nil
	}
}
