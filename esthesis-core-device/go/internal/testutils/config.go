package testutils

import (
	"fmt"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"os"
	"path/filepath"
	"strings"
	"testing"
)

// MockProperties Helper function to mock the properties and secure files for testing.
// It creates temporary files with the specified properties and sets the config flags accordingly.
func MockProperties(t *testing.T, props map[string]string) {
	t.Helper()

	tmpDir := t.TempDir()
	propertiesFile := filepath.Join(tmpDir, "esthesis.properties")
	secureFile := filepath.Join(tmpDir, "esthesis.secure.properties")

	var mainContent, secureContent string
	for key, value := range props {
		value := strings.ReplaceAll(value, "\n", "\\n")
		if key == config.RegistrationPropertyPrivateKey {
			secureContent += fmt.Sprintf("%s = %s\n", key, value)

		} else {
			mainContent += fmt.Sprintf("%s = %s\n", key, value)
		}
	}

	_ = os.WriteFile(propertiesFile, []byte(mainContent), 0644)
	_ = os.WriteFile(secureFile, []byte(secureContent), 0644)

	config.Flags.PropertiesFile = propertiesFile
	config.Flags.SecurePropertiesFile = secureFile
	config.InitRegistrationProperties()
}

// MockUpdateScript Mocks the update script.
func MockUpdateScript(t *testing.T) {
	t.Helper()
	config.Flags.ProvisioningScript =
		MockScriptFile(t, "firmware.sh", "#!/bin/sh\necho 'Updating firmware test'\n")
}

// MockScriptFile Mocks a script file with the given name and content.
func MockScriptFile(t *testing.T, scriptName string, scriptContent string) string {
	t.Helper()
	tmpDir := t.TempDir()
	scriptPath := filepath.Join(tmpDir, scriptName)
	scriptContentByte := []byte(scriptContent)
	err := os.WriteFile(scriptPath, scriptContentByte, 0755)
	if err != nil {
		t.Fatalf("Failed to create file: %v", err)
	}
	return scriptPath

}

// MockVersionFile Mocks the provisioning version file.
func MockVersionFile(t *testing.T) {
	t.Helper()

	tmpDir := t.TempDir()
	filePath := filepath.Join(tmpDir, "version.txt")
	content := []byte("1.0.0\n")
	err := os.WriteFile(filePath, content, 0755)
	if err != nil {
		t.Fatalf("Failed to create file: %v", err)
	}

	config.Flags.VersionFile = filePath

}
