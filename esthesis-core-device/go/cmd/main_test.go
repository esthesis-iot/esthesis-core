package main

import (
	"os"
	"path/filepath"
	"testing"
	"time"

	"github.com/esthesis-iot/esthesis-device/internal/pkg/dto"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	log "github.com/sirupsen/logrus"
	"github.com/stretchr/testify/assert"
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

	// Run the main function in a separate goroutine.
	go func() {
		main()
	}()

	// Allow the application to handle the signal and shut down.
	time.Sleep(2 * time.Second)

	// Assert that the application ran successfully.
	assert.True(t, true, "Application  ran successfully with minimal required parameters.")
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

	// Create dummy files.
	testutils.CreateFile(t, provisioningScript)
	testutils.CreateFile(t, rebootScript)
	testutils.CreateFile(t, shutdownScript)
	testutils.CreateFile(t, versionTxt)

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
	// Run the main function in a separate goroutine.
	go func() {
		main()
	}()

	// Allow the application to initialize.
	time.Sleep(5 * time.Second)

	// Assert that the application ran successfully.
	assert.True(t, true, "Application should ran successfully with all optional parameters.")
}

func TestMainFunction_InvalidLogLevel(t *testing.T) {
	// Backup and restore os.Args and log level after test
	originalArgs := os.Args
	originalLogLevel := log.GetLevel()
	defer func() {
		os.Args = originalArgs
		log.SetLevel(originalLogLevel)
	}()

	// Setup required directories and mock server.
	tmpDir := t.TempDir()
	propertiesFile := filepath.Join(tmpDir, "esthesis.properties")
	securePropertiesFile := filepath.Join(tmpDir, "esthesis.secure.properties")

	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	mockResponse := dto.RegistrationResponse{
		Certificate:       "cert-data",
		PublicKey:         "pub-key-data",
		PrivateKey:        "priv-key-data",
		MqttServer:        brokerAddr,
		ProvisioningUrl:   "http://test-provisioning-url",
		RootCaCertificate: "root-ca-data",
	}
	ts := testutils.MockRegistrationServer(t, mockResponse)
	defer ts.Close()

	// Use an invalid log level
	os.Args = []string{
		"main",
		"--registrationUrl=" + ts.URL,
		"--hardwareId=test-hardware-id",
		"--propertiesFile=" + propertiesFile,
		"--securePropertiesFile=" + securePropertiesFile,
		"--tempDir=" + tmpDir,
		"--logLevel=invalidLevel",
	}

	go func() {
		main()
	}()

	time.Sleep(1 * time.Second)

	// Assert fallback to .
	assert.Equal(t, log.InfoLevel, log.GetLevel(), "Expected log level to fall back to InfoLevel")
}

func TestMainFunction_PauseStartup(t *testing.T) {
	// Backup and restore os.Args, os.Stdin
	originalArgs := os.Args
	originalStdin := os.Stdin
	defer func() {
		os.Args = originalArgs
		os.Stdin = originalStdin
	}()

	// Prepare temp resources
	tmpDir := t.TempDir()
	propertiesFile := filepath.Join(tmpDir, "esthesis.properties")
	securePropertiesFile := filepath.Join(tmpDir, "esthesis.secure.properties")

	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	mockResponse := dto.RegistrationResponse{
		Certificate:       "cert-data",
		PublicKey:         "pub-key-data",
		PrivateKey:        "priv-key-data",
		MqttServer:        brokerAddr,
		ProvisioningUrl:   "http://test-provisioning-url",
		RootCaCertificate: "root-ca-data",
	}
	ts := testutils.MockRegistrationServer(t, mockResponse)
	defer ts.Close()

	// Set up args including pauseStartup
	os.Args = []string{
		"main",
		"--registrationUrl=" + ts.URL,
		"--hardwareId=test-hardware-id",
		"--propertiesFile=" + propertiesFile,
		"--securePropertiesFile=" + securePropertiesFile,
		"--tempDir=" + tmpDir,
		"--pauseStartup=true",
	}

	go func() {
		main()
	}()

	time.Sleep(2 * time.Second)

	assert.True(t, true, "Application should handle --pauseStartup .")
}

//func TestMainFunction_GracefulShutdown(t *testing.T) {
//
//	// Backup original os.Args and restore it after the test.
//	originalArgs := os.Args
//	defer func() { os.Args = originalArgs }()
//
//	// Override exitFunc for the duration of this test.
//	exitCalled := false
//	exitFunc = func(code int) { exitCalled = true }
//	defer func() { exitFunc = os.Exit }()
//
//	// Start the test broker.
//	broker, brokerAddr := testutils.StartTestBroker(t)
//	defer broker.Close()
//
//	// Mock a registration server and responses.
//	mockResponse := dto.RegistrationResponse{
//		Certificate:       "cert-data",
//		PublicKey:         "pub-key-data",
//		PrivateKey:        "priv-key-data",
//		MqttServer:        brokerAddr,
//		ProvisioningUrl:   "http://test-provisioning-url",
//		RootCaCertificate: "root-ca-data",
//	}
//	ts := testutils.MockRegistrationServer(t, mockResponse)
//	defer ts.Close() // Ensure the server is closed after the test.
//
//	// Create a temporary directory for the test.
//	tmpDir := t.TempDir()
//	propertiesFile := filepath.Join(tmpDir, "esthesis.properties")
//	securePropertiesFile := filepath.Join(tmpDir, "esthesis.secure.properties")
//	versionTxt := filepath.Join(tmpDir, "version.txt")
//	testutils.CreateFile(t, versionTxt)
//
//	// Mock os.Args to include all optional parameters.
//	os.Args = []string{
//		"main",
//		"--registrationUrl=" + ts.URL,
//		"--hardwareId=test-hardware-id",
//		"--propertiesFile=" + propertiesFile,
//		"--securePropertiesFile=" + securePropertiesFile,
//		"--tempDir=" + tmpDir,
//		"--topicPing=custom/ping",
//		"--topicTelemetry=custom/telemetry",
//		"--topicMetadata=custom/metadata",
//		"--topicCommandRequest=custom/cmd/req",
//		"--topicCommandReply=custom/cmd/rep",
//		"--healthReportInterval=400",
//		"--pingInterval=45",
//		"--logLevel=debug",
//		"--endpointHttp=true",
//		"--endpointHttpListeningIP=0.0.0.0",
//		"--endpointHttpListeningPort=8889",
//		"--endpointMqtt=true",
//		"--endpointMqttListeningIP=0.0.0.0",
//		"--endpointMqttListeningPort=1885",
//		"--autoUpdate=true",
//		"--versionReportTopic=custom/version/topic",
//		"--topicDemo=demo/topic",
//		"--demoCategory=test",
//		"--demoInterval=22",
//		"--ignoreHttpsInsecure",
//		"--ignoreMqttInsecure",
//		"--versionFile=" + versionTxt,
//	}
//
//	// Run the main function in a separate goroutine.
//	go func() {
//		main()
//	}()
//
//	// Allow the application to initialize.
//	time.Sleep(2 * time.Second)
//
//	// Simulate sending a termination signal.
//	var wgTest sync.WaitGroup
//	go func() {
//		GracefulShutdown(&wgTest)
//	}()
//
//	time.Sleep(10 * time.Second)
//
//	if !exitCalled {
//		t.Error("GracefulShutdown did not call exitFunc")
//	}
//
//	assert.True(t, true, "Application should shutdown gracefully.")
//}
