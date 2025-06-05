package luaExecutor

import (
	"github.com/stretchr/testify/assert"
	"os"
	"path/filepath"
	"testing"
)

func TestExecuteLuaScript(t *testing.T) {
	// Create a temporary directory for Lua scripts.
	tmpDir := t.TempDir()

	// Valid Lua script that modifies the payload.
	validLuaScript := `
		function transform(payload, endpoint)
			return "Modified: " .. payload
		end
		return transform(payload, endpoint)
	`
	validLuaScriptPath := filepath.Join(tmpDir, "valid_script.lua")
	err := os.WriteFile(validLuaScriptPath, []byte(validLuaScript), 0644)
	assert.NoError(t, err)

	// Invalid Lua script.
	invalidLuaScript := `
		function transform(payload, endpoint)
			return payload + 1 -- Invalid operation for string
		end
	`
	invalidLuaScriptPath := filepath.Join(tmpDir, "invalid_script.lua")
	err = os.WriteFile(invalidLuaScriptPath, []byte(invalidLuaScript), 0644)
	assert.NoError(t, err)

	// Test with a valid Lua script.
	result := ExecuteLuaScript("test-endpoint", "test-payload", validLuaScriptPath)
	assert.Equal(t, "Modified: test-payload", result, "Expected the payload to be modified by the Lua script")

	// Test with an invalid Lua script.
	result = ExecuteLuaScript("test-endpoint", "test-payload", invalidLuaScriptPath)
	assert.Equal(t, "test-payload", result, "Expected the original payload to be returned on Lua script error")

	// Test with a non-existent Lua script.
	result = ExecuteLuaScript("test-endpoint", "test-payload", "non_existent.lua")
	assert.Equal(t, "test-payload", result, "Expected the original payload to be returned when the Lua script file does not exist")
}
