package mqttClient

import (
	"testing"

	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
)

// TestConnect_Success tests the successful connection to the MQTT broker.
func TestConnect_Success(t *testing.T) {
	// Start a test MQTT broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Attempt to connect to the broker.
	ok := Connect()

	// Assert the connection was successful.
	assert.True(t, ok, "Expected successful connection to the MQTT broker.")
}

// TestConnect_Failure tests the failure to connect to the MQTT broker.
func TestConnect_NoServerConfigured(t *testing.T) {
	// Mock an empty MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: "",
	})

	// Attempt to connect to the broker.
	connected := Connect()

	// Assert the connection was not established.
	assert.False(t, connected, "Expected connection to fail due to no MQTT server configured.")
}

// TestDisconnect tests the disconnection from the MQTT broker.
func TestDisconnect(t *testing.T) {
	// Start a test MQTT broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Connect to the broker.
	Connect()

	// Disconnect from the broker.
	assert.NotPanics(t, func() {
		Disconnect()
	}, "Expected no panic during MQTT client disconnection.")
}

// TestPublish_Success tests the successful publishing of a message to the MQTT broker.
func TestPublish_Success(t *testing.T) {
	// Start a test MQTT broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Connect to the broker.
	Connect()
	defer Disconnect()

	// Publish a test message.
	topic := "test/topic"
	payload := []byte("test-message")
	ok := Publish(topic, payload)

	// Assert the publish operation was successful.
	assert.True(t, ok, "Expected a valid token for the publish operation.")
}

// TestGetTlsConfig tests the getTlsConfig function.
func TestGetTlsConfig(t *testing.T) {
	// Mock necessary properties for TLS configuration.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyRootCaCertificate: "-----BEGIN CERTIFICATE-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END CERTIFICATE-----",
		config.RegistrationPropertyCertificate:       "-----BEGIN CERTIFICATE-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END CERTIFICATE-----",
		config.RegistrationPropertyPrivateKey:        "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEA...\n-----END PRIVATE KEY-----",
	})

	// Call getTlsConfig and verify the result.
	tlsConfig := getTlsConfig()
	assert.NotNil(t, tlsConfig, "Expected a valid TLS configuration.")
	assert.Len(t, tlsConfig.Certificates, 1, "Expected one client certificate.")
	assert.NotNil(t, tlsConfig.RootCAs, "Expected a valid root CA pool.")
	assert.False(t, tlsConfig.InsecureSkipVerify, "Expected InsecureSkipVerify to be false by default.")
}
