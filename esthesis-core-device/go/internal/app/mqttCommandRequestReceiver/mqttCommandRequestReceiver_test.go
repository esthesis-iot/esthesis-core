package mqttCommandRequestReceiver

import (
	"github.com/esthesis-iot/esthesis-device/internal/pkg/appConstants"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
	"testing"
)

// Test_OnMessage_ExecuteCommandSync tests the execute command functionality of the MQTT command request receiver.
func Test_OnMessage_ExecuteCommand(t *testing.T) {
	// Start a test MQTT broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address and supported commands.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})
	// Initialize relevant configurations.
	config.Flags.SupportedCommands = string(appConstants.CommandTypeExec) +
		string(appConstants.CommandTypeFirmware) +
		string(appConstants.CommandTypeReboot) +
		string(appConstants.CommandTypeHealth) +
		string(appConstants.CommandTypeShutdown) +
		string(appConstants.CommandTypePing)

	// Create a test MQTT client.
	testMqttClient := testutils.CreateMqttClient(t, brokerAddr, "", "")

	// Define the  sync command request payload.
	commandPayloadSync := "123 es hostname arg1"

	// Define the async command request payload.
	commandPayloadAsync := "123 ea hostname arg1"

	// Create a mock MQTT message.
	mockMessageSync := &testutils.MockMessage{
		TopicName:   "test/topic",
		PayloadName: []byte(commandPayloadSync),
	}

	// Create a mock MQTT message for async command request.
	mockMessageAsync := &testutils.MockMessage{
		TopicName:   "test/topic",
		PayloadName: []byte(commandPayloadAsync),
	}

	// Assert that the OnMessage function does not panic when processing the sync command request.
	assert.NotPanics(t, func() {
		OnMessage(testMqttClient, mockMessageSync)
	})

	// Assert that the OnMessage function does not panic when processing the async command request.
	assert.NotPanics(t, func() {
		OnMessage(testMqttClient, mockMessageAsync)
	})

}

// Test_OnMessage_RebootCommand tests the reboot command functionality of the MQTT command request receiver.
func Test_OnMessage_RebootCommand(t *testing.T) {

	// Start a test MQTT broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Prepare a dummy reboot script.
	scriptPath := testutils.MockScriptFile(t,
		"reboot.sh",
		"#!/bin/sh\necho 'Reboot script executed'\n")

	// Mock config to point to this script.
	config.Flags.RebootScript = scriptPath

	// Mock MQTT server address and supported commands.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize relevant configurations.
	config.Flags.SupportedCommands = string(appConstants.CommandTypeExec) +
		string(appConstants.CommandTypeFirmware) +
		string(appConstants.CommandTypeReboot) +
		string(appConstants.CommandTypeHealth) +
		string(appConstants.CommandTypeShutdown) +
		string(appConstants.CommandTypePing)

	// Create a test MQTT client.
	testMqttClient := testutils.CreateMqttClient(t, brokerAddr, "", "")

	// Define the command request Sync payload.
	commandPayloadSync := "123 rs"

	// Define the command request Async payload.
	commandPayloadAsync := "123 ra"

	// Create a mock MQTT message.
	mockMessageSync := &testutils.MockMessage{
		TopicName:   "test/topic",
		PayloadName: []byte(commandPayloadSync),
	}

	// Create a mock MQTT message.
	mockMessageAsync := &testutils.MockMessage{
		TopicName:   "test/topic",
		PayloadName: []byte(commandPayloadAsync),
	}

	// Assert that the synchronous reboot command does not panic.
	assert.NotPanics(t, func() {
		OnMessage(testMqttClient, mockMessageSync)
	})

	// Assert that the asynchronous reboot command does not panic.
	assert.NotPanics(t, func() {
		OnMessage(testMqttClient, mockMessageAsync)
	})

}

// Test_OnMessage_ShutdownCommand tests the shutdown command functionality of the MQTT command request receiver.
func Test_OnMessage_ShutdownCommand(t *testing.T) {
	// Start a test MQTT broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Prepare a dummy shutdown script.
	scriptPath := testutils.MockScriptFile(t,
		"shutdown.sh",
		"#!/bin/sh\necho 'Shutdown script executed'\n")

	// Mock config to point to this script.
	config.Flags.ShutdownScript = scriptPath

	// Mock MQTT server address and supported commands.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize relevant configurations.
	config.Flags.SupportedCommands = string(appConstants.CommandTypeExec) +
		string(appConstants.CommandTypeFirmware) +
		string(appConstants.CommandTypeReboot) +
		string(appConstants.CommandTypeHealth) +
		string(appConstants.CommandTypeShutdown) +
		string(appConstants.CommandTypePing)

	// Create a test MQTT client.
	testMqttClient := testutils.CreateMqttClient(t, brokerAddr, "", "")

	// Define the shutdown command payload.
	commandPayload := "123 ss"

	// Create a mock MQTT message.
	mockMessage := &testutils.MockMessage{
		TopicName:   "test/topic",
		PayloadName: []byte(commandPayload),
	}

	// Assert that the shutdown command does not panic.
	assert.NotPanics(t, func() {
		OnMessage(testMqttClient, mockMessage)
	})
}

// Test_OnMessage_FirmwareCommand tests the firmware command functionality of the MQTT command request receiver.
func Test_OnMessage_FirmwareCommand(t *testing.T) {
	// Start a test MQTT broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address and supported commands.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize relevant configurations.
	config.Flags.SupportedCommands = string(appConstants.CommandTypeExec) +
		string(appConstants.CommandTypeFirmware) +
		string(appConstants.CommandTypeReboot) +
		string(appConstants.CommandTypeHealth) +
		string(appConstants.CommandTypeShutdown) +
		string(appConstants.CommandTypePing)

	// Create a test MQTT client.
	testMqttClient := testutils.CreateMqttClient(t, brokerAddr, "", "")

	// Define the firmware command payload.
	commandPayload := "123 fs"

	// Create a mock MQTT message.
	mockMessage := &testutils.MockMessage{
		TopicName:   "test/topic",
		PayloadName: []byte(commandPayload),
	}

	// Assert that the firmware command does not panic.
	assert.NotPanics(t, func() {
		OnMessage(testMqttClient, mockMessage)
	})
}

// ToDo Test_OnMessage_PingCommand.

// ToDo Test_OnMessage_HealthCommand.
