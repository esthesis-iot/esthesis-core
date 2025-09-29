package mqttPing

import (
	"time"

	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	log "github.com/sirupsen/logrus"
)

var pingTopic string

func Post() {
	payload := "health ping=" + time.Now().Format(time.RFC3339)
	mqttClient.Publish(pingTopic, []byte(payload))
}

func Start(done chan bool) {
	pingTopic = config.Flags.TopicPing + "/" + config.Flags.HardwareId
	log.Debugf("Starting Ping reporter at '%s'.", pingTopic)

	ticker := time.NewTicker(time.Duration(config.Flags.PingInterval) * time.Second)
	defer func() { ticker.Stop() }()
	Post()

LOOP:
	for {
		select {
		case doneMsg := <-done:
			if doneMsg {
				break LOOP
			} else {
				Post()
			}
		case <-ticker.C:
			Post()
		}
	}

	log.Debug("Ping reporter stopped.")
}
