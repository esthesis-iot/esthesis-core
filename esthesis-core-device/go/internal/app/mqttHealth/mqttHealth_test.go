package mqttHealth

import (
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
	"testing"
	"time"
)

func TestStart(t *testing.T) {

	// Start the test broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize relevant configurations.
	config.Flags.TopicTelemetry = "telemetry-test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.HealthReportInterval = 60

	// Initialize the MQTT client.
	mqttClient.Connect()

	// Assert that the Start function does not panic when starting the health reporter.
	assert.NotPanics(t, func() {
		done := make(chan bool)

		// Start health reporter in a goroutine.
		go Start(done)

		// Wait a moment then stop the reporter.
		time.Sleep(2 * time.Second)
		done <- true

		// Wait a bit more for graceful stop.
		time.Sleep(500 * time.Millisecond)

		// Test Post() directly.
		Post()
	})

}
