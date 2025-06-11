package demoData

import (
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
	"testing"
	"time"
)

func TestPost(t *testing.T) {
	// Start the test broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize relevant the configurations.
	config.InitRegistrationProperties()
	config.Flags.TopicDemo = "demo/test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5

	mqttClient.Connect()
	assert.NotPanics(t, func() {
		Post()
	})

	// Todo - Check the messages in the broker.

}

func TestStart(t *testing.T) {
	// Start the test broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize relevant the configurations.
	config.InitRegistrationProperties()
	config.Flags.TopicDemo = "demo/test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5
	config.Flags.DemoInterval = 5

	mqttClient.Connect()

	done := make(chan bool)

	// run new goroutine to avoid deadlock
	go func() {
		assert.NotPanics(t, func() {
			Start(done)
		})
	}()

	time.Sleep(2 * time.Second)
	done <- true
}
