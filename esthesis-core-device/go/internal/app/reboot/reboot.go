package reboot

import (
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	log "github.com/sirupsen/logrus"
	"os/exec"
)

func Reboot() {
	cmd := exec.Command(config.Flags.RebootScript)
	err := cmd.Start()
	if err != nil {
		log.WithError(err).Errorf("Could not execute reboot script.")
	} else {
		log.Info("Rebooting...")
	}
}
