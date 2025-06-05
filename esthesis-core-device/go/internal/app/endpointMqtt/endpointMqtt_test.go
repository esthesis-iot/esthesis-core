package endpointMqtt

import (
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
	"strconv"
	"testing"
	"time"
)

// TestStart_TelemetryTopic tests the telemetry topic of the MQTT endpoint.
func TestStart_TelemetryTopic(t *testing.T) {
	// Configure the MQTT server.
	config.Flags.EndpointMqttListeningIP = "127.0.0.1"
	port := testutils.GetFreePort(t)
	config.Flags.EndpointMqttListeningPort = port
	config.Flags.EndpointMqttAuthUsername = ""
	config.Flags.EndpointMqttAuthPassword = ""
	config.Flags.TopicTelemetry = "telemetry-test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5

	// Start the test broker which simulates the platform broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize device mqtt client configurations.
	mqttClient.Connect()

	// Start the Device MQTT server.
	done := make(chan bool)
	go Start(done)
	defer func() { done <- true }()

	// Wait briefly for the server to start.
	time.Sleep(200 * time.Millisecond)

	// Publish a test message to the telemetry topic in the Device Broker.
	deviceBrokerAddr := config.Flags.EndpointMqttListeningIP + ":" + strconv.Itoa(config.Flags.EndpointMqttListeningPort)
	testMqttClient := testutils.CreateMqttClient(t, deviceBrokerAddr, "", "")
	testPayload := "test-telemetry-data"
	token := testMqttClient.Publish(telemetryEndpoint, 0, false, testPayload)
	token.WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)

	// Assert no error occurred while publishing the telemetry data.
	assert.NoError(t, token.Error(), "Failed to publish telemetry data to the broker.")

}

// TestStart_TelemetryTopicWithAuth tests the telemetry topic of the MQTT endpoint with authentication.
func TestStart_TelemetryTopicWithAuth(t *testing.T) {
	// Configure the MQTT server.
	config.Flags.EndpointMqttListeningIP = "127.0.0.1"
	port := testutils.GetFreePort(t)
	config.Flags.EndpointMqttListeningPort = port
	config.Flags.EndpointMqttAuthUsername = "user-test"
	config.Flags.EndpointMqttAuthPassword = "pass-test"
	config.Flags.TopicTelemetry = "telemetry-test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5

	// Start the test broker which simulates the platform broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize device mqtt client configurations.
	mqttClient.Connect()

	// Start the Device MQTT server.
	done := make(chan bool)
	go Start(done)
	defer func() { done <- true }()

	// Wait briefly for the server to start.
	time.Sleep(200 * time.Millisecond)

	// Publish a test message to the telemetry topic in the Device Broker.
	deviceBrokerAddr := config.Flags.EndpointMqttListeningIP + ":" + strconv.Itoa(config.Flags.EndpointMqttListeningPort)
	testMqttClient := testutils.CreateMqttClient(t, deviceBrokerAddr, "user-test", "pass-test")
	testPayload := "test-telemetry-data"
	token := testMqttClient.Publish(telemetryEndpoint, 0, false, testPayload)
	token.WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)

	// Assert no error occurred while publishing the telemetry data.
	assert.NoError(t, token.Error(), "Failed to publish telemetry data to the broker.")

}

// TestStart_MetadataTopic tests the metadata topic of the MQTT endpoint.
func TestStart_MetadataTopic(t *testing.T) {
	// Configure the MQTT server.
	config.Flags.EndpointMqttListeningIP = "127.0.0.1"
	port := testutils.GetFreePort(t)
	config.Flags.EndpointMqttListeningPort = port
	config.Flags.EndpointMqttAuthUsername = ""
	config.Flags.EndpointMqttAuthPassword = ""
	config.Flags.TopicMetadata = "metadata-test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5

	// Start the test broker which simulates the platform broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize device mqtt client configurations.
	mqttClient.Connect()

	// Start the Device MQTT server.
	done := make(chan bool)
	go Start(done)
	defer func() { done <- true }()

	// Wait briefly for the server to start.
	time.Sleep(200 * time.Millisecond)

	// Publish a test message to the metadata topic in the Device Broker.
	deviceBrokerAddr := config.Flags.EndpointMqttListeningIP + ":" + strconv.Itoa(config.Flags.EndpointMqttListeningPort)
	testMqttClient := testutils.CreateMqttClient(t, deviceBrokerAddr, "", "")
	testPayload := "test-metadata-data"
	token := testMqttClient.Publish(metadataEndpoint, 0, false, testPayload)
	token.WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)

	// Assert no error occurred while publishing the metadata data.
	assert.NoError(t, token.Error(), "Failed to publish metadata data to the broker.")
}

// TestStart_MetadataTopicWithAuth tests the metadata topic of the MQTT endpoint with authentication.
func TestStart_MetadataTopicWithAuth(t *testing.T) {
	// Configure the MQTT server.
	config.Flags.EndpointMqttListeningIP = "127.0.0.1"
	port := testutils.GetFreePort(t)
	config.Flags.EndpointMqttListeningPort = port
	config.Flags.EndpointMqttAuthUsername = "user-test"
	config.Flags.EndpointMqttAuthPassword = "pass-test"
	config.Flags.TopicMetadata = "metadata-test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5

	// Start the test broker which simulates the platform broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize device mqtt client configurations.
	mqttClient.Connect()

	// Start the Device MQTT server.
	done := make(chan bool)
	go Start(done)
	defer func() { done <- true }()

	// Wait briefly for the server to start.
	time.Sleep(200 * time.Millisecond)

	// Publish a test message to the metadata topic in the Device Broker.
	deviceBrokerAddr := config.Flags.EndpointMqttListeningIP + ":" + strconv.Itoa(config.Flags.EndpointMqttListeningPort)
	testMqttClient := testutils.CreateMqttClient(t, deviceBrokerAddr, "user-test", "pass-test")
	testPayload := "test-metadata-data"
	token := testMqttClient.Publish(metadataEndpoint, 0, false, testPayload)
	token.WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)

	// Assert no error occurred while publishing the metadata data.
	assert.NoError(t, token.Error(), "Failed to publish metadata data to the broker.")
}

// TestStart_CustomTelemetryTopic tests the custom telemetry topic of the MQTT endpoint.
func TestStart_CustomTelemetryTopic(t *testing.T) {
	// Configure the MQTT server.
	config.Flags.EndpointMqttListeningIP = "127.0.0.1"
	port := testutils.GetFreePort(t)
	config.Flags.EndpointMqttListeningPort = port
	config.Flags.EndpointMqttAuthUsername = ""
	config.Flags.EndpointMqttAuthPassword = ""
	config.Flags.TopicTelemetry = "telemetry-test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5
	config.Flags.LuaExtraMqttTelemetryTopic = []string{"custom/telemetry", "customTelemetryHandler"}

	// Start the test broker which simulates the platform broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize device mqtt client configurations.
	mqttClient.Connect()

	// Start the Device MQTT server.
	done := make(chan bool)
	go Start(done)
	defer func() { done <- true }()

	// Wait briefly for the server to start.
	time.Sleep(200 * time.Millisecond)

	// Publish a test message to the custom telemetry topic in the Device Broker.
	deviceBrokerAddr := config.Flags.EndpointMqttListeningIP + ":" + strconv.Itoa(config.Flags.EndpointMqttListeningPort)
	testMqttClient := testutils.CreateMqttClient(t, deviceBrokerAddr, "", "")
	testPayload := "test-custom-telemetry-data"
	token := testMqttClient.Publish("custom/telemetry", 0, false, testPayload)
	token.WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)

	// Assert no error occurred while publishing the custom telemetry data.
	assert.NoError(t, token.Error(), "Failed to publish custom telemetry data to the broker.")
}

// TestStart_CustomMetadataTopic tests the custom metadata topic of the MQTT endpoint.
func TestStart_CustomMetadataTopic(t *testing.T) {
	// Configure the MQTT server.
	config.Flags.EndpointMqttListeningIP = "127.0.0.1"
	port := testutils.GetFreePort(t)
	config.Flags.EndpointMqttListeningPort = port
	config.Flags.EndpointMqttAuthUsername = ""
	config.Flags.EndpointMqttAuthPassword = ""
	config.Flags.TopicMetadata = "metadata-test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5
	config.Flags.LuaExtraMqttMetadataTopic = []string{"custom/metadata", "customMetadataHandler"}

	// Start the test broker which simulates the platform broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize device mqtt client configurations.
	mqttClient.Connect()

	// Start the Device MQTT server.
	done := make(chan bool)
	go Start(done)
	defer func() { done <- true }()

	// Wait briefly for the server to start.
	time.Sleep(200 * time.Millisecond)

	// Publish a test message to the custom metadata topic in the Device Broker.
	deviceBrokerAddr := config.Flags.EndpointMqttListeningIP + ":" + strconv.Itoa(config.Flags.EndpointMqttListeningPort)
	testMqttClient := testutils.CreateMqttClient(t, deviceBrokerAddr, "", "")
	testPayload := "test-custom-metadata-data"
	token := testMqttClient.Publish("custom/metadata", 0, false, testPayload)
	token.WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)

	// Assert no error occurred while publishing the custom metadata data.
	assert.NoError(t, token.Error(), "Failed to publish custom metadata data to the broker.")
}
