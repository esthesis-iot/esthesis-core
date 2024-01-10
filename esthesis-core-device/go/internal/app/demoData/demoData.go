package demoData

import (
	"fmt"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	log "github.com/sirupsen/logrus"
	"math/rand"
	"strings"
	"time"
)

var demoTopic string
var demoCategory string
var gpsIndex = 0
var gps = [][]float64{
	{37.969508103781045, 23.723613674890164},
	{37.96754710547837, 23.723796821902397},
	{37.965541001467216, 23.720797803004658},
	{37.965182762840314, 23.71688999050155},
	{37.9695531546778, 23.71529960169218},
	{37.97446057967671, 23.71861669835181},
	{37.97750516749656, 23.721115880766547},
	{37.97599075467541, 23.727475539139192},
	{37.973711562539584, 23.73098413767146},
	{37.97102253699923, 23.732121183492318},
	{37.967667519409105, 23.730399371249817},
}

func randomInt(min, max int) int {
	return min + rand.Intn(max-min)
}

func randomFloat(min, max float64) float64 {
	return min + rand.Float64()*(max-min)
}

func randomGps() []float64 {
	gpsIndex++
	if gpsIndex >= len(gps) {
		gpsIndex = 0
	}
	return gps[gpsIndex]
}

func Post() {
	randomGps := randomGps()
	randomCpuTemperature := randomFloat(0, 100)
	randomCpuLoad := randomFloat(0, 100)
	randomThreads := randomInt(256, 1025)
	randomProcesses := randomInt(0, 100)
	randomMemory := randomFloat(0, 100)
	randomDisk := randomFloat(0, 100)
	randomNetwork := randomFloat(0, 100)
	randomBattery := randomFloat(0, 100)
	randomBatteryTemperature := randomFloat(0, 100)
	randomBatteryVoltage := randomFloat(0, 100)
	randomBatteryCurrent := randomFloat(0, 100)

	payload := demoCategory + " " + strings.Join([]string{
		"cpu_temperature=" + fmt.Sprintf("%.2f", randomCpuTemperature) + "f",
		"cpu_load=" + fmt.Sprintf("%.2f", randomCpuLoad) + "f",
		"threads=" + fmt.Sprintf("%d", randomThreads) + "i",
		"processes=" + fmt.Sprintf("%d", randomProcesses) + "i",
		"memory=" + fmt.Sprintf("%.2f", randomMemory) + "f",
		"disk=" + fmt.Sprintf("%.2f", randomDisk) + "f",
		"network=" + fmt.Sprintf("%.2f", randomNetwork) + "f",
		"battery=" + fmt.Sprintf("%.2f", randomBattery) + "f",
		"battery_temperature=" + fmt.Sprintf("%.2f", randomBatteryTemperature) + "f",
		"battery_voltage=" + fmt.Sprintf("%.2f", randomBatteryVoltage) + "f",
		"battery_current=" + fmt.Sprintf("%.2f", randomBatteryCurrent) + "f",
		"gps_lat=" + fmt.Sprintf("%.15f", randomGps[0]) + "f",
		"gps_lon=" + fmt.Sprintf("%.15f", randomGps[1]) + "f",
	}, ",")

	mqttClient.Publish(demoTopic,
		[]byte(payload)).WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)
}

func Start(done chan bool) {
	demoTopic = config.Flags.TopicDemo + "/" + config.Flags.HardwareId
	demoCategory = config.Flags.DemoCategory
	log.Debugf("Starting Demo reporter at '%s'.", demoTopic)

	ticker := time.NewTicker(time.Duration(config.Flags.DemoInterval) * time.Second)
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

	log.Debug("Demo reporter stopped.")
}
