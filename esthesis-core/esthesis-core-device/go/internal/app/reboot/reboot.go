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
		log.Errorf("Could not execute reboot script due to '%s'.", err)
	} else {
		log.Info("Rebooting...")
	}
}
