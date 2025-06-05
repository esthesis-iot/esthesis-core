package reboot

import (
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestReboot(t *testing.T) {

	// Prepare a dummy reboot script.
	scriptPath := testutils.MockScriptFile(t,
		"reboot.sh",
		"#!/bin/sh\necho 'Reboot script executed'\n")

	// Mock config to point to this script.
	config.Flags.RebootScript = scriptPath

	// Assert that the Reboot function does not panic.
	assert.NotPanics(t, func() {
		Reboot()
	})

}
