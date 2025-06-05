package util

import (
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/stretchr/testify/assert"
	"os"
	"path/filepath"
	"testing"
)

func TestAbbrBA(t *testing.T) {
	config.Flags.LogAbbreviation = 10
	input := []byte("This is a long message")
	expected := "This is a ..."
	assert.Equal(t, expected, AbbrBA(input))
}

func TestAbbrSA(t *testing.T) {
	config.Flags.LogAbbreviation = 15
	input := []string{"This", "is", "a", "long", "message"}
	expected := "This is a long ..."
	assert.Equal(t, expected, AbbrSA(input))
}

func TestAbbrS(t *testing.T) {
	config.Flags.LogAbbreviation = 8
	input := "This is a long message"
	expected := "This is ..."
	assert.Equal(t, expected, AbbrS(input))
}

func TestReadTextFile(t *testing.T) {
	tmpFile := filepath.Join(t.TempDir(), "test.txt")
	content := "Hello, World!"
	err := os.WriteFile(tmpFile, []byte(content), 0644)
	assert.NoError(t, err)

	readContent, err := ReadTextFile(tmpFile)
	assert.NoError(t, err)
	assert.Equal(t, content, readContent)
}

func TestIsFileExists(t *testing.T) {
	tmpFile := filepath.Join(t.TempDir(), "test.txt")
	err := os.WriteFile(tmpFile, []byte("content"), 0644)
	assert.NoError(t, err)

	assert.True(t, IsFileExists(tmpFile))
	assert.False(t, IsFileExists(filepath.Join(t.TempDir(), "nonexistent.txt")))
}

func TestIsFirmwareVersionFilePresent(t *testing.T) {
	tmpFile := filepath.Join(t.TempDir(), "version.txt")
	config.Flags.VersionFile = tmpFile

	err := os.WriteFile(tmpFile, []byte("1.0.0"), 0644)
	assert.NoError(t, err)

	assert.True(t, IsFirmwareVersionFilePresent())

	os.Remove(tmpFile)
	assert.False(t, IsFirmwareVersionFilePresent())
}

func TestGetFirmwareVersion(t *testing.T) {
	tmpFile := filepath.Join(t.TempDir(), "version.txt")
	config.Flags.VersionFile = tmpFile

	content := "1.0.0\n"
	err := os.WriteFile(tmpFile, []byte(content), 0644)
	assert.NoError(t, err)

	version := GetFirmwareVersion()
	assert.Equal(t, "1.0.0", version)
}
