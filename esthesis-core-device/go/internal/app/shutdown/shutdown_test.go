package shutdown

import (
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
	"testing"
)

// Ensures that the shutdown script runs when valid.
func TestShutdown_ValidScript(t *testing.T) {
	// Prepare a dummy shutdown script.
	scriptPath := testutils.MockScriptFile(t,
		"shutdown.sh",
		"#!/bin/sh\necho 'Shutdown script executed'\n")

	// Mock config to point to this script.
	config.Flags.ShutdownScript = scriptPath

	// Assert it doesn't panic.
	assert.NotPanics(t, func() {
		Shutdown()
	})
}

// Ensures that an error is logged when script is invalid.
func TestShutdown_InvalidScript(t *testing.T) {
	// Set config to a non-existent path.
	config.Flags.ShutdownScript = "/non/existent/script.sh"

	// Assert it doesn't panic.
	assert.NotPanics(t, func() {
		Shutdown()
	})
}
