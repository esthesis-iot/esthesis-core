package config

import (
	"fmt"
	"github.com/stretchr/testify/assert"
	"os"
	"path/filepath"
	"testing"
)

// TestInitRegistrationProperties tests the InitRegistrationProperties function to ensure it correctly loads properties from the specified files.
func TestInitRegistrationProperties(t *testing.T) {
	// Prepare temporary files for properties and secure properties.
	tmpDir := t.TempDir()
	propertiesFile := filepath.Join(tmpDir, "esthesis.properties")
	secureFile := filepath.Join(tmpDir, "esthesis.secure.properties")

	// Create content for the properties and secure properties files.
	var mainContent, secureContent string

	secureContent += fmt.Sprintf("%s = %s\n", RegistrationPropertyCertificate, "test-cert")
	secureContent += fmt.Sprintf("%s = %s\n", RegistrationPropertyPrivateKey, "test-private-key")
	secureContent += fmt.Sprintf("%s = %s\n", RegistrationPropertyPublicKey, "test-public-key")
	mainContent += fmt.Sprintf("%s = %s\n", RegistrationPropertyMqttServer, "mqtt-server")
	mainContent += fmt.Sprintf("%s = %s\n", RegistrationPropertyProvisioningUrl, "test-provisioning-url")

	// Write the content to the respective files.
	_ = os.WriteFile(propertiesFile, []byte(mainContent), 0644)
	_ = os.WriteFile(secureFile, []byte(secureContent), 0644)

	// Set the properties and secure properties file paths in the Flags.
	Flags.PropertiesFile = propertiesFile
	Flags.SecurePropertiesFile = secureFile

	// Call InitRegistrationProperties.
	assert.NotPanics(t, func() {
		InitRegistrationProperties()
	}, "Expected InitRegistrationProperties to not panic")

	// Assert properties are loaded correctly.
	assert.Equal(t, "mqtt-server", GetRegistrationProperty(RegistrationPropertyMqttServer))
	assert.Equal(t, "test-provisioning-url", GetRegistrationProperty(RegistrationPropertyProvisioningUrl))
	assert.Equal(t, "test-cert", GetRegistrationProperty(RegistrationPropertyCertificate))
	assert.Equal(t, "test-private-key", GetRegistrationProperty(RegistrationPropertyPrivateKey))
	assert.Equal(t, "test-public-key", GetRegistrationProperty(RegistrationPropertyPublicKey))
}

// TestInitCmdFlags_MinimalRequired tests the minimal required flags for the InitCmdFlags function.
func TestInitCmdFlags_MinimalRequired(t *testing.T) {
	args := []string{
		"esthesis-core-device",
		"--registrationUrl=http://localhost:8080/register",
		"--hardwareId=device123",
	}

	// Backup and defer restore of env vars to avoid side effects
	oldArgs := os.Args
	defer func() { os.Args = oldArgs }()

	InitCmdFlags(args)

	if Flags.RegistrationURL != "http://localhost:8080/register" {
		t.Errorf("Expected RegistrationURL to be set, got: %s", Flags.RegistrationURL)
	}
	if Flags.HardwareId != "device123" {
		t.Errorf("Expected HardwareId to be set, got: %s", Flags.HardwareId)
	}

	// Check some defaults
	if Flags.HttpTimeout != 60 {
		t.Errorf("Expected default HttpTimeout to be 60, got: %d", Flags.HttpTimeout)
	}
}

// TestInitCmdFlags_AllOptionalFlags tests the InitCmdFlags function with all optional flags set.
func TestInitCmdFlags_AllOptionalFlags(t *testing.T) {
	tempDir := os.TempDir()

	args := []string{
		"esthesis-core-device",
		"--registrationUrl=http://localhost:1234/register",
		"--hardwareId=test-device",
		"--pauseStartup",
		"--propertiesFile=/tmp/properties.properties",
		"--securePropertiesFile=/tmp/secure.properties",
		"--tempDir=" + tempDir,
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
		"--autoUpdate",
		"--secureProvisioning",
		"--signatureAlgorithm=SHA256WITHRSA",
		"--versionFile=/tmp/version.txt",
		"--versionReport",
		"--versionReportTopic=custom/version/topic",
		"--provisioningScript=/tmp/provision.sh",
		"--rebootScript=/tmp/reboot.sh",
		"--shutdownScript=/tmp/shutdown.sh",
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
		// Lua extras
		"--luaExtraMqttTelemetryTopic", "topic1", "script1.lua",
		"--luaExtraMqttMetadataTopic", "topic2", "script2.lua",
		"--luaExtraHttpTelemetryEndpoint", "http://localhost:8081/telemetry", "http_telemetry.lua",
		"--luaExtraHttpMetadataEndpoint", "http://localhost:8081/metadata", "http_metadata.lua",
	}

	InitCmdFlags(args)

	assert := func(field string, got, want interface{}) {
		if got != want {
			t.Errorf("Expected %s=%v, got %v", field, want, got)
		}
	}

	assert("Flags.PauseStartup", Flags.PauseStartup, true)
	assert("Flags.HttpTimeout", Flags.HttpTimeout, 100)
	assert("Flags.MqttTimeout", Flags.MqttTimeout, 90)
	assert("Flags.RetryHttpRequest", Flags.RetryHttpRequest, 30)
	assert("Flags.TopicPing", Flags.TopicPing, "custom/ping")
	assert("Flags.TopicTelemetry", Flags.TopicTelemetry, "custom/telemetry")
	assert("Flags.TopicMetadata", Flags.TopicMetadata, "custom/metadata")
	assert("Flags.TopicCommandRequest", Flags.TopicCommandRequest, "custom/cmd/req")
	assert("Flags.TopicCommandReply", Flags.TopicCommandReply, "custom/cmd/rep")
	assert("Flags.HealthReportInterval", Flags.HealthReportInterval, 400)
	assert("Flags.PingInterval", Flags.PingInterval, 45)
	assert("Flags.LogLevel", Flags.LogLevel, "debug")
	assert("Flags.LogAbbreviation", Flags.LogAbbreviation, 256)
	assert("Flags.Tags", Flags.Tags, "tag1,tag2")
	assert("Flags.EndpointHttp", Flags.EndpointHttp, true)
	assert("Flags.EndpointHttpListeningIP", Flags.EndpointHttpListeningIP, "0.0.0.0")
	assert("Flags.EndpointHttpListeningPort", Flags.EndpointHttpListeningPort, 8888)
	assert("Flags.EndpointHttpAuthUsername", Flags.EndpointHttpAuthUsername, "user")
	assert("Flags.EndpointHttpAuthPassword", Flags.EndpointHttpAuthPassword, "pass")
	assert("Flags.EndpointMqtt", Flags.EndpointMqtt, true)
	assert("Flags.EndpointMqttListeningIP", Flags.EndpointMqttListeningIP, "0.0.0.0")
	assert("Flags.EndpointMqttListeningPort", Flags.EndpointMqttListeningPort, 1884)
	assert("Flags.EndpointMqttAuthUsername", Flags.EndpointMqttAuthUsername, "mqttUser")
	assert("Flags.EndpointMqttAuthPassword", Flags.EndpointMqttAuthPassword, "mqttPass")
	assert("Flags.AutoUpdate", Flags.AutoUpdate, true)
	assert("Flags.SecureProvisioning", Flags.SecureProvisioning, true)
	assert("Flags.SignatureAlgorithm", Flags.SignatureAlgorithm, "SHA256WITHRSA")
	assert("Flags.VersionFile", Flags.VersionFile, "/tmp/version.txt")
	assert("Flags.VersionReport", Flags.VersionReport, true)
	assert("Flags.VersionReportTopic", Flags.VersionReportTopic, "custom/version/topic")
	assert("Flags.ProvisioningScript", Flags.ProvisioningScript, "/tmp/provision.sh")
	assert("Flags.RebootScript", Flags.RebootScript, "/tmp/reboot.sh")
	assert("Flags.ShutdownScript", Flags.ShutdownScript, "/tmp/shutdown.sh")
	assert("Flags.SupportedCommands", Flags.SupportedCommands, "erpsh")
	assert("Flags.TopicDemo", Flags.TopicDemo, "demo/topic")
	assert("Flags.DemoCategory", Flags.DemoCategory, "test")
	assert("Flags.DemoInterval", Flags.DemoInterval, 22)
	assert("Flags.RegistrationSecret", Flags.RegistrationSecret, "mysecret")
	assert("Flags.Attributes", Flags.Attributes, "key1=val1,key2=val2")
	assert("Flags.IgnoreHttpsInsecure", Flags.IgnoreHttpsInsecure, true)
	assert("Flags.IgnoreMqttInsecure", Flags.IgnoreMqttInsecure, true)
	assert("Flags.MqttInflightTTLDuration", Flags.MqttInflightTTLDuration, 120)
	assert("Flags.LuaHttpTelemetryScript", Flags.LuaHttpTelemetryScript, "http_telemetry.lua")
	assert("Flags.LuaHttpMetadataScript", Flags.LuaHttpMetadataScript, "http_metadata.lua")
	assert("Flags.LuaMqttTelemetryScript", Flags.LuaMqttTelemetryScript, "mqtt_telemetry.lua")
	assert("Flags.LuaMqttMetadataScript", Flags.LuaMqttMetadataScript, "mqtt_metadata.lua")

	if len(Flags.LuaExtraMqttTelemetryTopic) != 2 ||
		Flags.LuaExtraMqttTelemetryTopic[0] != "topic1" ||
		Flags.LuaExtraMqttTelemetryTopic[1] != "script1.lua" {
		t.Errorf("Flags.LuaExtraMqttTelemetryTopic not parsed correctly: %+v", Flags.LuaExtraMqttTelemetryTopic)
	}
	if len(Flags.LuaExtraHttpMetadataEndpoint) != 2 ||
		Flags.LuaExtraHttpMetadataEndpoint[0] != "http://localhost:8081/metadata" ||
		Flags.LuaExtraHttpMetadataEndpoint[1] != "http_metadata.lua" {
		t.Errorf("Flags.LuaExtraHttpMetadataEndpoint not parsed correctly: %+v", Flags.LuaExtraHttpMetadataEndpoint)
	}
}
