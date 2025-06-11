package cryptoUtil

import (
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
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

func TestParseRsaPrivateKeyFromPemStr(t *testing.T) {
	// Generate a new private key for testing.
	privPEM := testutils.GeneratePrivateKeyPEM()

	// Call the function under test.
	parsedKey, err := ParseRsaPrivateKeyFromPemStr(privPEM)
	if err != nil {
		t.Fatalf("ParseRsaPrivateKeyFromPemStr returned error: %v", err)
	}
	if parsedKey == nil {
		t.Fatal("Expected non-nil parsed key")
	}
}

func TestSign(t *testing.T) {

	privPEM := testutils.GeneratePrivateKeyPEM()
	content := "test content for signing"

	signature, err := Sign(privPEM, content)
	if err != nil {
		t.Fatalf("Sign returned error: %v", err)
	}
	if len(signature) == 0 {
		t.Fatal("Expected non-empty signature")
	}

	// Verify the signature can be parsed.
	_, err = ParseRsaPrivateKeyFromPemStr(privPEM)
	if err != nil {
		t.Fatalf("Failed to parse private key from PEM: %v", err)
	}
}
