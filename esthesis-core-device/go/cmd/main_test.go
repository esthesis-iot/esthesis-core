package main

import (
	"os"
	"path/filepath"
	"sync"
	"testing"
	"time"

	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/dto"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	log "github.com/sirupsen/logrus"
	"github.com/stretchr/testify/assert"
)

var wgTest sync.WaitGroup

func TestMainFunction_MinimalRequired(t *testing.T) {
	// Backup original os.Args and restore it after the test.
	originalArgs := os.Args
	defer func() { os.Args = originalArgs }()

	// Setup to capture log output.
	logHook := testutils.NewLogHook()
	log.AddHook(logHook)

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
		"--hardwareId=test-hardware-id-1",
		"--propertiesFile=" + propertiesFile,
		"--securePropertiesFile=" + securePropertiesFile,
		"--tempDir=" + tmpDir,
	}

	// Run the main function in a separate goroutine.
	go main()

	// Allow the application to initialize.
	time.Sleep(5 * time.Second)

	// Assert hardware ID is set correctly.
	assert.Equal(t, "test-hardware-id-1", config.Flags.HardwareId, "Hardware ID should match the provided value")
	// Assert that the startup log message is present.
	assert.True(t, logHook.Contains("Device agent started."), "Expected startup message to appear in logs")

}

func TestMainFunction_AllOptionalParameters(t *testing.T) {
	// Backup original os.Args and restore it after the test.
	originalArgs := os.Args
	defer func() { os.Args = originalArgs }()

	// Setup to capture log output.
	logHook := testutils.NewLogHook()
	log.AddHook(logHook)

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
		"--hardwareId=test-hardware-id-2",
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
	go main()

	// Allow the application to initialize.
	time.Sleep(5 * time.Second)

	// Assert hardware ID is set correctly.
	assert.Equal(t, "test-hardware-id-2", config.Flags.HardwareId, "Hardware ID should match the provided value")
	// Assert that the startup log message is present.
	assert.True(t, logHook.Contains("Device agent started."), "Expected startup message to appear in logs")

}

func TestMainFunction_InvalidLogLevel(t *testing.T) {
	// Backup and restore os.Args and log level after test.
	originalArgs := os.Args
	originalLogLevel := log.GetLevel()
	defer func() {
		os.Args = originalArgs
		log.SetLevel(originalLogLevel)
	}()

	// Setup to capture log output.
	logHook := testutils.NewLogHook()
	log.AddHook(logHook)

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
		"--hardwareId=test-hardware-id-3",
		"--propertiesFile=" + propertiesFile,
		"--securePropertiesFile=" + securePropertiesFile,
		"--tempDir=" + tmpDir,
		"--logLevel=invalidLevel",
	}

	go main()

	// Allow the application to initialize.
	time.Sleep(5 * time.Second)

	// Assert hardware ID is set correctly.
	assert.Equal(t, "test-hardware-id-3", config.Flags.HardwareId, "Hardware ID should match the provided value")
	// Assert fallback to InfoLevel
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

	// Setup to capture log output.
	logHook := testutils.NewLogHook()
	log.AddHook(logHook)

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

	// Set up args including pauseStartup.
	os.Args = []string{
		"main",
		"--registrationUrl=" + ts.URL,
		"--hardwareId=test-hardware-id-4",
		"--propertiesFile=" + propertiesFile,
		"--securePropertiesFile=" + securePropertiesFile,
		"--tempDir=" + tmpDir,
		"--pauseStartup",
	}

	go main()

	// Allow some time to ensure the application is paused at startup.
	time.Sleep(5 * time.Second)

	// Assert paused config is set.
	assert.True(t, config.Flags.PauseStartup, "PauseStartup flag should be true")

}

func TestMainFunction_GracefulShutdown(t *testing.T) {

	// Backup original os.Args and restore it after the test.
	originalArgs := os.Args
	defer func() { os.Args = originalArgs }()

	// override exitFunc during the test so os.Exit isn’t called.
	origExit := exitFunc
	exitCalled := false
	exitFunc = func(code int) { exitCalled = true }
	defer func() { exitFunc = origExit }()

	// Setup to capture log output.
	logHook := testutils.NewLogHook()
	log.AddHook(logHook)

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
	versionTxt := filepath.Join(tmpDir, "version.txt")
	testutils.CreateFile(t, versionTxt)

	// Mock os.Args to include all optional parameters.
	os.Args = []string{
		"main",
		"--registrationUrl=" + ts.URL,
		"--hardwareId=test-hardware-id-5",
		"--propertiesFile=" + propertiesFile,
		"--securePropertiesFile=" + securePropertiesFile,
		"--tempDir=" + tmpDir,
		"--topicPing=custom/ping",
		"--topicTelemetry=custom/telemetry",
		"--topicMetadata=custom/metadata",
		"--topicCommandRequest=custom/cmd/req",
		"--topicCommandReply=custom/cmd/rep",
		"--healthReportInterval=400",
		"--pingInterval=45",
		"--logLevel=debug",
		"--endpointHttp=true",
		"--endpointHttpListeningIP=0.0.0.0",
		"--endpointHttpListeningPort=8889",
		"--endpointMqtt=true",
		"--endpointMqttListeningIP=0.0.0.0",
		"--endpointMqttListeningPort=1885",
		"--autoUpdate=true",
		"--versionReportTopic=custom/version/topic",
		"--topicDemo=demo/topic",
		"--demoCategory=test",
		"--demoInterval=22",
		"--ignoreHttpsInsecure",
		"--ignoreMqttInsecure",
		"--versionFile=" + versionTxt,
	}

	// Run the main function in a separate goroutine.
	go main()

	// Allow the application to initialize.
	time.Sleep(5 * time.Second)

	// Make sure to call GracefulShutdown to clean up.
	//var wgTest sync.WaitGroup
	GracefulShutdown(&wgTest)

	time.Sleep(10 * time.Second)

	// Assert hardware ID is set correctly.
	assert.Equal(t, "test-hardware-id-5", config.Flags.HardwareId, "Hardware ID should match the provided value")
	// Assert that the startup log message is present.
	assert.True(t, logHook.Contains("Device agent started."), "Expected startup message to appear in logs")
	// Assert that the shutdown log message is present.
	assert.True(t, logHook.Contains("Graceful shutdown completed."), "Expected shutdown completion message to appear in logs")
	assert.True(t, exitCalled, "Application should shutdown gracefully.")
}
