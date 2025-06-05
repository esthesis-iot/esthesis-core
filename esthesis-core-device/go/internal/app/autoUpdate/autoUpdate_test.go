package autoUpdate

import (
	"errors"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/appConstants"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
	"testing"
)

// Ensures that the Update function returns an error when the provisioning URL is not set.
func TestUpdate_NoProvisioningURL(t *testing.T) {
	updateInProgress = false

	// Mock Provisioning URL as empty.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyProvisioningUrl: "",
	})

	msg, err := Update("")

	// Assert update failed with expected error.
	assert.Empty(t, msg)
	assert.Error(t, err)
	assert.True(t, errors.Is(err, err), "Expected a non-nil error if provisioning URL is not set")
	assert.Contains(t, err.Error(), "No provisioning URL set")
	assert.False(t, IsUpdateInProgress())
}

// Ensures that the Update function returns an error another update is in progress.
func TestUpdate_UpdateInProgress(t *testing.T) {
	// Set updateInProgress to true to simulate an ongoing update.
	updateInProgress = true

	// Mock Provisioning URL as valid.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyProvisioningUrl: "test-url",
	})

	msg, err := Update("")

	// Assert update failed with expected error.
	assert.Empty(t, msg)
	assert.Error(t, err)
	assert.True(t, errors.Is(err, err), "Expected a non-nil error if update is in progress.")
	assert.Contains(t, err.Error(), "Update already in progress")
	assert.True(t, IsUpdateInProgress())
}

// Ensures that the Update function returns an error when the update handling script is missing.
func TestUpdate_UpdateScriptMissing(t *testing.T) {
	updateInProgress = false

	// Mock Provisioning URL as valid.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyProvisioningUrl: "test-url",
	})

	msg, err := Update("")

	// Assert update failed with expected error.
	assert.Empty(t, msg)
	assert.Error(t, err)
	assert.True(t, errors.Is(err, err), "Expected a non-nil error if update handling script is missing.")
	assert.Contains(t, err.Error(), "update handling script not found")
	assert.False(t, IsUpdateInProgress())
}

// Ensures that the Update function returns an error when the current firmware version is unknown.
func TestUpdate_UnknownFirmwareVersion(t *testing.T) {
	updateInProgress = false

	// Mock Provisioning URL as valid.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyProvisioningUrl: "test-url",
	})

	// Mock the existence of a provisioning script.
	testutils.MockUpdateScript(t)

	msg, err := Update("")

	// Assert update failed with expected error.
	assert.Empty(t, msg)
	assert.Error(t, err)
	assert.True(t, errors.Is(err, err), "Expected a non-nil error if current firmware version is unknown.")
	assert.Contains(t, err.Error(), "Current firmware version is unknown")
	assert.False(t, IsUpdateInProgress())
}

// Ensures that the Update function returns an error when the provisioning URL is invalid.
func TestUpdate_InvalidProvisioningURL(t *testing.T) {
	updateInProgress = false

	// Mock Provisioning URL  with an invalid format.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyProvisioningUrl: "://invalid-url",
	})

	// Mock the existence of a provisioning script.
	testutils.MockUpdateScript(t)

	// Mock the existence of a version file.
	testutils.MockVersionFile(t)

	msg, err := Update("")

	// Assert update failed with expected error.
	assert.Empty(t, msg)
	assert.Error(t, err)
	assert.True(t, errors.Is(err, err), "Expected a non-nil error if provisioning URL is invalid.")
	assert.Contains(t, err.Error(), "Could not parse provisioning URL")
	assert.False(t, IsUpdateInProgress())
}

// Tests the Update function with all necessary conditions met for internal provisioning.
func TestUpdate_SuccessInternal(t *testing.T) {
	updateInProgress = false

	// Mock Provisioning server and response.
	server := testutils.MockProvisioningServer(t, testutils.MockProvisioningServerOpts{
		ResponseCode:     200,
		ProvisioningType: appConstants.ProvisioningPackageTypeInternal,
		DownloadContent:  []byte("test file content"),
	})
	defer server.Close()

	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyProvisioningUrl: server.URL,
	})

	// Mock the existence of a provisioning script.
	testutils.MockUpdateScript(t)

	// Mock the existence of a version file.
	testutils.MockVersionFile(t)

	msg, err := Update("")

	// Assert update failed with expected error.
	assert.NotEmpty(t, msg)
	assert.NoError(t, err)
	assert.Contains(t, msg, "Firmware update initiated")
	assert.False(t, IsUpdateInProgress())

	testutils.CleanUpTempFilesInCurrentPackage(t)
}

// Tests the Update function with all necessary conditions met for external provisioning.
func TestUpdate_SuccessExternal(t *testing.T) {
	updateInProgress = false

	// Mock Provisioning server and response.
	server := testutils.MockProvisioningServer(t, testutils.MockProvisioningServerOpts{
		ResponseCode:     200,
		ProvisioningType: appConstants.ProvisioningPackageTypeExternal,
		DownloadContent:  []byte("test file content"),
	})
	defer server.Close()

	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyProvisioningUrl: server.URL,
	})

	// Mock the existence of a provisioning script.
	testutils.MockUpdateScript(t)

	// Mock the existence of a version file.
	testutils.MockVersionFile(t)

	msg, err := Update("")

	// Assert update failed with expected error.
	assert.NotEmpty(t, msg)
	assert.NoError(t, err)
	assert.Contains(t, msg, "Firmware update initiated")
	assert.False(t, IsUpdateInProgress())

	testutils.CleanUpTempFilesInCurrentPackage(t)
}

// Ensures that the Update function returns an error when the provisioning type is unknown.
func TestUpdate_UnknowProvisioningType(t *testing.T) {
	updateInProgress = false

	// Mock Provisioning server and response.
	server := testutils.MockProvisioningServer(t, testutils.MockProvisioningServerOpts{
		ResponseCode:     200,
		ProvisioningType: "unknown",
		DownloadContent:  []byte("test file content"),
	})
	defer server.Close()

	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyProvisioningUrl: server.URL,
	})

	// Mock the existence of a provisioning script.
	testutils.MockUpdateScript(t)

	// Mock the existence of a version file.
	testutils.MockVersionFile(t)

	msg, err := Update("")

	// Assert update failed with expected error.
	assert.Empty(t, msg)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "Unknown provisioning type")
	assert.False(t, IsUpdateInProgress())

	testutils.CleanUpTempFilesInCurrentPackage(t)
}

func TestUpdate_ProvisioningServerError(t *testing.T) {
	updateInProgress = false

	// Mock Provisioning server and response.
	server := testutils.MockProvisioningServer(t, testutils.MockProvisioningServerOpts{
		ResponseCode:     500,
		ProvisioningType: appConstants.ProvisioningPackageTypeInternal,
	})
	defer server.Close()

	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyProvisioningUrl: server.URL,
	})
	testutils.MockUpdateScript(t)
	testutils.MockVersionFile(t)

	msg, err := Update("")

	assert.Empty(t, msg)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "Could not get provisioning info due to")
	assert.False(t, IsUpdateInProgress())
}
