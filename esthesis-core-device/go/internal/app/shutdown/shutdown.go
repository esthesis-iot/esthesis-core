package shutdown

import (
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	log "github.com/sirupsen/logrus"
	"os/exec"
)

func Shutdown() {
	cmd := exec.Command(config.Flags.ShutdownScript)
	err := cmd.Start()
	if err != nil {
		log.WithError(err).Errorf("Could not execute shutdown script.")
	} else {
		log.Info("Shutting down...")
	}
}
