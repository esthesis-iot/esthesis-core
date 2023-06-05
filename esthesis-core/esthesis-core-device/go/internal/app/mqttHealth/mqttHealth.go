package mqttHealth

import (
	"fmt"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/mackerelio/go-osstat/loadavg"
	"github.com/mackerelio/go-osstat/memory"
	log "github.com/sirupsen/logrus"
	"strconv"
	"time"
)

var healthTopic string

func Post() {
	payload := getHealthInfo()
	mqttClient.Publish(healthTopic,
		[]byte(payload)).WaitTimeout(time.Duration(config.Flags.
		MqttTimeout) * time.Second)
}
func appendLoadHealth(healthInfo *string) {
	loadavg, err := loadavg.Get()

	if err != nil {
		log.Errorf("%s", err)
	} else {
		*healthInfo += "loadAverage1=" + fmt.Sprintf("%v",
			loadavg.Loadavg1) + "d,"
		*healthInfo += "loadAverage5=" + fmt.Sprintf("%v",
			loadavg.Loadavg5) + "d,"
		*healthInfo += "loadAverage15=" + fmt.Sprintf("%v",
			loadavg.Loadavg15) + "d"
	}
}

func appendMemoryHealth(healthInfo *string) {
	memoryStats, err := memory.Get()

	if err != nil {
		log.Errorf("%s", err)
	} else {
		*healthInfo += "memoryTotal=" + strconv.FormatUint(memoryStats.Total,
			10) + "l,"
		*healthInfo += "memoryUsed=" + strconv.FormatUint(memoryStats.Used,
			10) + "l,"
		*healthInfo += "memoryFree=" + strconv.FormatUint(memoryStats.Free,
			10) + "l"
	}
}

func getHealthInfo() string {
	healthInfo := "health "
	appendMemoryHealth(&healthInfo)
	healthInfo += ","
	appendLoadHealth(&healthInfo)

	return healthInfo
}

func Start(done chan bool) {
	healthTopic = config.Flags.TopicTelemetry + "/" + config.Flags.HardwareId
	log.Debugf("Starting Health reporter at '%s'.", healthTopic)

	ticker := time.NewTicker(time.Duration(
		config.Flags.HealthReportInterval) * time.Second)
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

	log.Debugf("Health reporter stopped.")
}
