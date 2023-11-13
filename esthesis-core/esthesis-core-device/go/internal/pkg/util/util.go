package util

import (
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	log "github.com/sirupsen/logrus"
	"io/ioutil"
	"os"
	"strings"
)

// AbbrBA abbreviates a byte array to the length specified in the config.
func AbbrBA(msg []byte) string {
	return AbbrS(string(msg))
}

// AbbrSA abbreviates a byte array to the length specified in the config.
func AbbrSA(str []string) string {
	return AbbrS(strings.Join(str, " "))
}

// AbbrS abbreviates a string to the length specified in the config.
func AbbrS(str string) string {
	if len(str) > config.Flags.LogAbbreviation {
		return str[:config.Flags.LogAbbreviation] + "..."
	}

	return str
}

// ReadTextFile reads a text file and returns its content as a string.
func ReadTextFile(filename string) (string, error) {
	file, err := ioutil.ReadFile(filename)
	if err != nil {
		log.WithError(err).Errorf("Could not read file'%s'.", filename)
	}
	return string(file), err
}

func IsFileExists(filename string) bool {
	_, err := os.Stat(filename)
	return err == nil
}

func IsFirmwareVersionFilePresent() bool {
	return IsFileExists(config.Flags.VersionFile)
}

func GetFirmwareVersion() string {
	version, _ := ReadTextFile(config.Flags.VersionFile)
	// Remove spaces and new lines.
	version = strings.TrimSpace(version)
	version = strings.Replace(version, "\n", "", -1)
	version = strings.Replace(version, "\r", "", -1)

	return version
}
