package cryptoUtil

import (
	"crypto"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/hex"
	"encoding/pem"
	"errors"
	log "github.com/sirupsen/logrus"
	"io"
	"os"
)

func ParseRsaPrivateKeyFromPemStr(privPEM string) (*rsa.PrivateKey, error) {
	block, err1 := pem.Decode([]byte(privPEM))
	if err1 == nil {
		return nil, errors.New("failed to parse PEM block containing the key")
	}

	priv, err2 := x509.ParsePKCS8PrivateKey(block.Bytes)
	if err2 != nil {
		return nil, err2
	}

	return priv.(*rsa.PrivateKey), nil
}

func Hash(content string) []byte {
	hash := sha256.Sum256(([]byte)(content))
	return hash[:]
}

func HashEncoded(content string) string {
	hash := sha256.Sum256(([]byte)(content))
	return hex.EncodeToString(hash[:])
}

func HashFileEncoded(filename string) (string, error) {
	file, err := os.Open(filename)
	if err != nil {
		return "", err
	}
	defer file.Close()

	hash := sha256.New()
	if _, err := io.Copy(hash, file); err != nil {
		return "", err
	}

	return hex.EncodeToString(hash.Sum(nil)), nil
}

func Sign(privateKeyPEM string, content string) (string, error) {
	log.Debugf("Signing content '%s' with private key '%s'.", content, privateKeyPEM)
	privateKey, err := ParseRsaPrivateKeyFromPemStr(privateKeyPEM)
	if err != nil {
		log.Debugf("Could not parse private key due to '%s'.", err)
		return "", errors.New("could not parse private key")
	}

	// Create a SHA256 hash of the message
	msgHashSum := Hash(content)

	signature, err := rsa.SignPKCS1v15(rand.Reader, privateKey, crypto.SHA256, msgHashSum)
	// signature, err := rsa.SignPSS(rand.Reader, privateKey, crypto.SHA256, msgHashSum, nil)
	if err != nil {
		log.Debugf("Could not sign content due to '%s'.", err)
		return "", errors.New("could not generate signature")
	}

	return base64.StdEncoding.EncodeToString(signature), nil
}
