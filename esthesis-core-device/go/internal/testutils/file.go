package testutils

import (
	log "github.com/sirupsen/logrus"
	"os"
	"path/filepath"
	"testing"
)

// CleanUpTempFilesInCurrentPackage Delete all non-*.go files in the current package directory.
func CleanUpTempFilesInCurrentPackage(t *testing.T) {
	t.Helper()

	err := filepath.Walk(".", func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if !info.IsDir() && filepath.Ext(path) != ".go" {
			return os.Remove(path)
		}
		return nil
	})

	if err != nil {
		log.Warn("Failed to clean up temporary files: " + err.Error())
	}

}

// createFile creates a temporary file in the current directory for testing purposes.
func CreateFile(t *testing.T, filename string) *os.File {
	t.Helper()

	file, err := os.Create(filename)
	if err != nil {
		t.Fatalf("Failed to create file %s: %v", filename, err)
	}

	t.Cleanup(func() {
		err := file.Close()
		if err != nil {
			log.Warnf("Failed to close file %s: %v", filename, err)
		}
	})

	return file
}
