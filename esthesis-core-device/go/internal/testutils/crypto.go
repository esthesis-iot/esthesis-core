package testutils

import (
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/pem"
)

func GeneratePrivateKeyPEM() string {
	// Generate a new RSA private key for testing.
	privateKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		panic("Failed to generate private key: " + err.Error())
	}

	// Encode the private key to PKCS#8 ASN.1 DER format.
	privBytes, err := x509.MarshalPKCS8PrivateKey(privateKey)
	if err != nil {
		panic("Failed to marshal private key: " + err.Error())
	}

	// Encode to PEM.
	privPEM := pem.EncodeToMemory(&pem.Block{
		Type:  "PRIVATE KEY",
		Bytes: privBytes,
	})

	return string(privPEM)
}
