package endpointHttp

import (
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"net/http"
	"strconv"
	"strings"
	"testing"
	"time"

	"github.com/esthesis-iot/esthesis-device/internal/pkg/buffer"
)

// TestStart_Telemetry tests the telemetry endpoint of the HTTP server.
func TestStart_Telemetry(t *testing.T) {
	config.Flags.EndpointHttpListeningIP = "127.0.0.1"
	port := testutils.GetFreePort(t)
	config.Flags.EndpointHttpListeningPort = port
	config.Flags.EndpointHttpAuthUsername = ""
	config.Flags.EndpointHttpAuthPassword = ""

	// Start the test broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize relevant the configurations.
	config.InitRegistrationProperties()
	config.Flags.TopicTelemetry = "telemetry-test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5

	// Initialize the MQTT client.
	mqttClient.Connect()

	// Start the HTTP server.
	done := make(chan bool)
	buf := buffer.NewInMemoryBuffer(buffer.Options{})
	go Start(done, buf)
	defer func() { done <- true }()

	// Wait briefly for server to start.
	time.Sleep(200 * time.Millisecond)

	payload := `{"deviceId":"test","cpu":0.42}`
	resp, err := http.Post("http://127.0.0.1:"+strconv.Itoa(port)+"/telemetry", "application/json", strings.NewReader(payload))
	require.NoError(t, err)
	defer resp.Body.Close()

	// Assert the response status code is 200 OK.
	assert.Equal(t, http.StatusOK, resp.StatusCode)
}

// TestStart_Metadata tests the metadata endpoint of the HTTP server.
func TestStart_Metadata(t *testing.T) {
	config.Flags.EndpointHttpListeningIP = "127.0.0.1"
	port := testutils.GetFreePort(t)
	config.Flags.EndpointHttpListeningPort = port
	config.Flags.EndpointHttpAuthUsername = ""
	config.Flags.EndpointHttpAuthPassword = ""

	// Start the test broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize relevant configurations.
	config.InitRegistrationProperties()
	config.Flags.TopicMetadata = "metadata-test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5

	// Initialize the MQTT client.
	mqttClient.Connect()

	// Start the HTTP server.
	done := make(chan bool)
	buf := buffer.NewInMemoryBuffer(buffer.Options{})
	go Start(done, buf)
	defer func() { done <- true }()

	// Wait briefly for server to start.
	time.Sleep(200 * time.Millisecond)

	payload := `{"deviceId":"test","info":"metadata"}`
	resp, err := http.Post("http://127.0.0.1:"+strconv.Itoa(port)+"/metadata", "application/json", strings.NewReader(payload))
	require.NoError(t, err)
	defer resp.Body.Close()

	// Assert the response status code is 200 OK.
	assert.Equal(t, http.StatusOK, resp.StatusCode)
}

// TestStart_CustomTelemetryEndpoint tests custom telemetry endpoints defined in the configuration.
func TestStart_CustomTelemetryEndpoint(t *testing.T) {
	config.Flags.EndpointHttpListeningIP = "127.0.0.1"
	port := testutils.GetFreePort(t)
	config.Flags.EndpointHttpListeningPort = port
	config.Flags.EndpointHttpAuthUsername = ""
	config.Flags.EndpointHttpAuthPassword = ""

	// Start the test broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize relevant configurations.
	config.InitRegistrationProperties()
	config.Flags.TopicTelemetry = "telemetry-test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5
	config.Flags.LuaExtraHttpTelemetryEndpoint = []string{"/custom-telemetry", ""}

	// Initialize the MQTT client.
	mqttClient.Connect()

	// Start the HTTP server.
	done := make(chan bool)
	buf := buffer.NewInMemoryBuffer(buffer.Options{})
	go Start(done, buf)
	defer func() { done <- true }()

	// Wait briefly for server to start.
	time.Sleep(200 * time.Millisecond)

	payload := `{"deviceId":"test","customTelemetry":true}`
	resp, err := http.Post("http://127.0.0.1:"+strconv.Itoa(port)+"/custom-telemetry", "application/json", strings.NewReader(payload))
	require.NoError(t, err)
	defer resp.Body.Close()

	// Assert the response status code is 200 OK.
	assert.Equal(t, http.StatusOK, resp.StatusCode)
}

// TestStart_CustomMetadataEndpoint tests custom metadata endpoints defined in the configuration.
func TestStart_CustomMetadataEndpoint(t *testing.T) {
	config.Flags.EndpointHttpListeningIP = "127.0.0.1"
	port := testutils.GetFreePort(t)
	config.Flags.EndpointHttpListeningPort = port
	config.Flags.EndpointHttpAuthUsername = ""
	config.Flags.EndpointHttpAuthPassword = ""

	// Start the test broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize relevant configurations.
	config.InitRegistrationProperties()
	config.Flags.TopicMetadata = "metadata-test"
	config.Flags.HardwareId = "test-hardware-id"
	config.Flags.MqttTimeout = 5
	config.Flags.LuaExtraHttpMetadataEndpoint = []string{"/custom-metadata", ""}

	// Initialize the MQTT client.
	mqttClient.Connect()

	// Start the HTTP server.
	done := make(chan bool)
	buf := buffer.NewInMemoryBuffer(buffer.Options{})
	go Start(done, buf)
	defer func() { done <- true }()

	// Wait briefly for server to start.
	time.Sleep(200 * time.Millisecond)

	payload := `{"deviceId":"test","customMetadata":true}`
	resp, err := http.Post("http://127.0.0.1:"+strconv.Itoa(port)+"/custom-metadata", "application/json", strings.NewReader(payload))
	require.NoError(t, err)
	defer resp.Body.Close()

	// Assert the response status code is 200 OK.
	assert.Equal(t, http.StatusOK, resp.StatusCode)
}
