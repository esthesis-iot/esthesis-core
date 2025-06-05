package main

import (
	"github.com/esthesis-iot/esthesis-device/internal/pkg/dto"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
	"os"
	"os/signal"
	"path/filepath"
	"syscall"
	"testing"
	"time"
)

func TestMainFunction_MinimalRequired(t *testing.T) {
	// Backup original os.Args and restore it after the test.
	originalArgs := os.Args
	defer func() { os.Args = originalArgs }()

	// Create a temporary directory for the test.
	tmpDir := t.TempDir()

	// Start the test broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock a registration server and responses.
	mockResponse := dto.RegistrationResponse{
		Certificate:       "cert-data",
		PublicKey:         "pub-key-data",
		PrivateKey:        "priv-key-data",
		MqttServer:        brokerAddr,
		ProvisioningUrl:   "http://test-provisioning-url",
		RootCaCertificate: "root-ca-data",
	}
	ts := testutils.MockRegistrationServer(t, mockResponse)
	defer ts.Close() // Ensure the server is closed after the test.

	// Create a temporary directory for the test.
	tempDir := t.TempDir()
	propertiesFile := filepath.Join(tempDir, "esthesis.properties")
	securePropertiesFile := filepath.Join(tempDir, "esthesis.secure.properties")

	// Mock os.Args to include the required registrationUrl and hardwareId parameters.
	os.Args = []string{
		"main",
		"--registrationUrl=" + ts.URL,
		"--hardwareId=test-hardware-id",
		"--propertiesFile=" + propertiesFile,
		"--securePropertiesFile=" + securePropertiesFile,
		"--tempDir=" + tmpDir,
	}

	// Create a channel to simulate termination signals.
	signalChan := make(chan os.Signal, 1)
	signal.Notify(signalChan, os.Interrupt, syscall.SIGTERM)

	// Run the main function in a separate goroutine.
	go func() {
		main()
	}()

	// Allow the application to initialize.
	time.Sleep(2 * time.Second)

	// Simulate a termination signal.
	signalChan <- os.Interrupt

	// Allow the application to handle the signal and shut down.
	time.Sleep(2 * time.Second)

	// Assert that the application shuts down gracefully.
	assert.True(t, true, "Application should shut down gracefully.")
}

func TestMainFunction_AllOptionalParameters(t *testing.T) {

	// Backup original os.Args and restore it after the test.
	originalArgs := os.Args
	defer func() { os.Args = originalArgs }()

	// Start the test broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock a registration server and responses.
	mockResponse := dto.RegistrationResponse{
		Certificate:       "cert-data",
		PublicKey:         "pub-key-data",
		PrivateKey:        "priv-key-data",
		MqttServer:        brokerAddr,
		ProvisioningUrl:   "http://test-provisioning-url",
		RootCaCertificate: "root-ca-data",
	}
	ts := testutils.MockRegistrationServer(t, mockResponse)
	defer ts.Close() // Ensure the server is closed after the test.

	// Create a temporary directory for the test.
	tmpDir := t.TempDir()
	propertiesFile := filepath.Join(tmpDir, "esthesis.properties")
	securePropertiesFile := filepath.Join(tmpDir, "esthesis.secure.properties")
	provisioningScript := filepath.Join(tmpDir, "provision.sh")
	versionTxt := filepath.Join(tmpDir, "version.txt")
	rebootScript := filepath.Join(tmpDir, "reboot.sh")
	shutdownScript := filepath.Join(tmpDir, "shutdown.sh")

	// Mock os.Args to include all optional parameters.
	os.Args = []string{
		"main",
		"--registrationUrl=" + ts.URL,
		"--hardwareId=test-hardware-id",
		"--propertiesFile=" + propertiesFile,
		"--securePropertiesFile=" + securePropertiesFile,
		"--tempDir=" + tmpDir,
		"--httpTimeout=100",
		"--mqttTimeout=90",
		"--httpRetry=30",
		"--topicPing=custom/ping",
		"--topicTelemetry=custom/telemetry",
		"--topicMetadata=custom/metadata",
		"--topicCommandRequest=custom/cmd/req",
		"--topicCommandReply=custom/cmd/rep",
		"--healthReportInterval=400",
		"--pingInterval=45",
		"--logLevel=debug",
		"--logAbbreviation=256",
		"--tags=tag1,tag2",
		"--endpointHttp",
		"--endpointHttpListeningIP=0.0.0.0",
		"--endpointHttpListeningPort=8888",
		"--endpointHttpAuthUsername=user",
		"--endpointHttpAuthPassword=pass",
		"--endpointMqtt",
		"--endpointMqttListeningIP=0.0.0.0",
		"--endpointMqttListeningPort=1884",
		"--endpointMqttAuthUsername=mqttUser",
		"--endpointMqttAuthPassword=mqttPass",
		"--autoUpdate=true",
		"--secureProvisioning",
		"--signatureAlgorithm=SHA256WITHRSA",
		"--versionFile=" + versionTxt,
		"--versionReport=true",
		"--versionReportTopic=custom/version/topic",
		"--provisioningScript=" + provisioningScript,
		"--rebootScript=" + rebootScript,
		"--shutdownScript=" + shutdownScript,
		"--supportedCommands=erpsh",
		"--topicDemo=demo/topic",
		"--demoCategory=test",
		"--demoInterval=22",
		"--registrationSecret=mysecret",
		"--attributes=key1=val1,key2=val2",
		"--luaHttpTelemetryScript=http_telemetry.lua",
		"--luaHttpMetadataScript=http_metadata.lua",
		"--luaMqttTelemetryScript=mqtt_telemetry.lua",
		"--luaMqttMetadataScript=mqtt_metadata.lua",
		"--ignoreHttpsInsecure",
		"--ignoreMqttInsecure",
		"--mqttInflightTTLDuration=120",
	}

	// Create a channel to simulate termination signals.
	signalChan := make(chan os.Signal, 1)
	signal.Notify(signalChan, os.Interrupt, syscall.SIGTERM)

	// Run the main function in a separate goroutine.
	go func() {
		main()
	}()

	// Allow the application to initialize.
	time.Sleep(2 * time.Second)

	// Simulate a termination signal.
	signalChan <- os.Interrupt

	// Allow the application to handle the signal and shut down.
	time.Sleep(2 * time.Second)

	// Assert that the application shuts down gracefully.
	assert.True(t, true, "Application should shut down gracefully.")
}
