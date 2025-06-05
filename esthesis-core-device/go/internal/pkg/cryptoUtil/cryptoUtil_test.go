package cryptoUtil

import (
	"os"
	"testing"
)

func TestHashFunctions(t *testing.T) {
	text := "test hash"
	hash := Hash(text)
	encoded := HashEncoded(text)

	if len(hash) != 32 {
		t.Errorf("Expected SHA256 hash length of 32, got %d", len(hash))
	}
	if len(encoded) != 64 {
		t.Errorf("Expected hex-encoded SHA256 string length of 64, got %d", len(encoded))
	}
}

func TestHashFileEncoded(t *testing.T) {
	content := "test file hash"
	tmpFile, err := os.CreateTemp("", "hash-test-*.txt")
	if err != nil {
		t.Fatal(err)
	}
	defer os.Remove(tmpFile.Name())

	_, _ = tmpFile.WriteString(content)
	_ = tmpFile.Close()

	hash, err := HashFileEncoded(tmpFile.Name())
	if err != nil {
		t.Errorf("HashFileEncoded returned an error: %v", err)
	}
	if len(hash) != 64 {
		t.Errorf("Expected SHA256 hex string length of 64, got %d", len(hash))
	}
}
