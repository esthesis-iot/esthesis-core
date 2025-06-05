package registration

import (
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/dto"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
	"os"
	"path/filepath"
	"testing"
)

// Checks if the Register function returns false when the properties and secure files already exist.
// The files existing is an indicator that the device is already registered.
func TestRegister_WhenFileExist(t *testing.T) {
	// Mock the properties file and secure file and their respective configurations .
	tmpDir := t.TempDir()
	propertiesFile := filepath.Join(tmpDir, "esthesis.properties")
	secureFile := filepath.Join(tmpDir, "esthesis.secure.properties")
	config.Flags.PropertiesFile = propertiesFile
	config.Flags.SecurePropertiesFile = secureFile

	// Create dummy files to simulate existing files.
	_ = os.WriteFile(propertiesFile, []byte("test"), 0644)
	_ = os.WriteFile(secureFile, []byte("test"), 0644)

	// Call the Register function and assert it returns false.
	result := Register()
	assert.False(t, result, "Expected Register to return false when files exist.")
}

// Checks if the Register function returns true when the properties and secure files do not exist.
// Checks also if the files are created with the expected content.
func TestRegister_Success(t *testing.T) {
	// Setup temp directory and expected file paths.
	tmpDir := t.TempDir()
	propertiesFile := filepath.Join(tmpDir, "esthesis.properties")
	secureFile := filepath.Join(tmpDir, "esthesis-secure.properties")

	// Mock a registration server and responses.
	mockResponse := dto.RegistrationResponse{
		Certificate:       "cert-data",
		PublicKey:         "pub-key-data",
		PrivateKey:        "priv-key-data",
		MqttServer:        "mqtt://broker",
		ProvisioningUrl:   "http://test-provisioning-url",
		RootCaCertificate: "root-ca-data",
	}
	ts := testutils.MockRegistrationServer(t, mockResponse)
	defer ts.Close() // Ensure the server is closed after the test.

	//Set flags used in registration.
	config.Flags.PropertiesFile = propertiesFile
	config.Flags.SecurePropertiesFile = secureFile
	config.Flags.HardwareId = "test-device"
	config.Flags.RegistrationURL = ts.URL
	config.Flags.TlsVerification = false
	config.Flags.IgnoreHttpsInsecure = true
	config.Flags.HttpTimeout = 2
	config.Flags.RetryHttpRequest = 1

	// Call Register and assert it's successful.
	registered := Register()
	assert.True(t, registered, "Expected successful registration.")

	// Read and verify properties file.
	props, err := os.ReadFile(propertiesFile)
	assert.NoError(t, err)
	assert.Contains(t, string(props), "Certificate = cert-data")
	assert.Contains(t, string(props), "PublicKey = pub-key-data")
	assert.Contains(t, string(props), "MqttServer = mqtt://broker")
	assert.Contains(t, string(props), "ProvisioningUrl = http://test-provisioning-url")
	assert.Contains(t, string(props), "RootCaCertificate = root-ca-data")

	// Read and verify secure properties file.
	secureProps, err := os.ReadFile(secureFile)
	assert.NoError(t, err)
	assert.Contains(t, string(secureProps), "PrivateKey = priv-key-data")
}
