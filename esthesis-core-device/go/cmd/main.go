package main

import (
	"fmt"
	"github.com/esthesis-iot/esthesis-device/internal/app/autoUpdate"
	"github.com/esthesis-iot/esthesis-device/internal/app/demoData"
	"github.com/esthesis-iot/esthesis-device/internal/app/endpointHttp"
	"github.com/esthesis-iot/esthesis-device/internal/app/endpointMqtt"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttHealth"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttPing"
	"github.com/esthesis-iot/esthesis-device/internal/app/registration"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/banner"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/channels"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/util"
	log "github.com/sirupsen/logrus"
	"os"
	"os/signal"
	"sync"
	"syscall"
	"time"
)

var wg sync.WaitGroup

func main() {
	// Print application banner.
	banner.Print(config.Version)

	// Read CLI flags and display an error if mandatory arguments are missing.
	config.InitCmdFlags(os.Args[1:])

	// Setup logger.
	log.SetOutput(os.Stdout)
	switch config.Flags.LogLevel {
	case "debug":
		log.SetLevel(log.DebugLevel)
	case "info":
		log.SetLevel(log.InfoLevel)
	case "trace":
		log.SetLevel(log.TraceLevel)
		log.SetReportCaller(true)
	default:
		log.SetLevel(log.InfoLevel)
	}
	log.SetFormatter(&log.TextFormatter{TimestampFormat: "2006-01-02 15:04:05", FullTimestamp: true})

	// Wait at initialisation.
	if config.Flags.PauseStartup {
		fmt.Print("Press 'Enter' to continue...")
		_, _ = fmt.Scanln()
	}

	// Termination signal handler.
	c := make(chan os.Signal)
	signal.Notify(c, os.Interrupt, syscall.SIGTERM)
	go func() {
		<-c
		channels.Shutdown()
		log.Info("Stopping device agent gracefully.")
		if channels.IsEndpointHttpChan() {
			channels.GetEndpointHttpChan() <- true
		}
		if channels.IsEndpointMqttChan() {
			channels.GetEndpointMqttChan() <- true
		}
		if channels.IsHealthChan() {
			channels.GetHealthChan() <- true
		}
		if channels.IsPingChan() {
			channels.GetPingChan() <- true
		}
		if channels.IsDemoChan() {
			channels.GetDemoChan() <- true
		}
		if channels.IsAutoUpdateChan() {
			channels.GetAutoUpdateChan() <- true
		}
		log.Debug("Disconnecting MQTT client.")
		mqttClient.Disconnect()
		wg.Wait()
		log.Info("Graceful shutdown completed.")
		os.Exit(0)
	}()

	// Register device.
	registration.Register()
	config.InitRegistrationProperties()

	log.Debugf("Using device properties file '%s'.", config.Flags.PropertiesFile)
	log.Debugf("Using device secure properties file '%s'.", config.Flags.SecurePropertiesFile)
	log.Debugf("Using temporary directory '%s'.", config.Flags.TempDir)

	// Connect to MQTT broker.
	mqttServerConnected := mqttClient.Connect()

	// Check if device firmware version should be reported.
	if config.Flags.VersionReport && config.Flags.VersionFile != "" && mqttServerConnected {
		if !util.IsFirmwareVersionFilePresent() {
			log.Warnf("Version file '%s' not found. "+
				"Device firmware version will not be reported.", config.Flags.VersionFile)
		} else {
			version := util.GetFirmwareVersion()
			versionMsg := "health firmware='" + version + "'"
			log.Infof("Reporting device firmware version '%s'.", versionMsg)
			mqttClient.Publish(config.Flags.VersionReportTopic+"/"+config.Flags.HardwareId,
				[]byte(versionMsg))
		}
	}

	// Startup ping and health reporters.
	if config.Flags.PingInterval > 0 && mqttServerConnected {
		wg.Add(1)
		go func() {
			defer wg.Done()
			mqttPing.Start(channels.GetPingChan())
		}()
	}
	if config.Flags.HealthReportInterval > 0 && mqttServerConnected {
		wg.Add(1)
		go func() {
			defer wg.Done()
			mqttHealth.Start(channels.GetHealthChan())
		}()
	}

	// Startup embedded HTTP server.
	if config.Flags.EndpointHttp && mqttServerConnected {
		wg.Add(1)
		go func() {
			defer wg.Done()
			endpointHttp.Start(channels.GetEndpointHttpChan())
		}()
	}

	// Startup embedded MQTT server.
	if config.Flags.EndpointMqtt && mqttServerConnected {
		wg.Add(1)
		go func() {
			defer wg.Done()
			endpointMqtt.Start(channels.GetEndpointMqttChan())
		}()
	}

	// Startup auto update.
	if config.Flags.AutoUpdate && mqttServerConnected {
		if !util.IsFileExists(config.Flags.VersionFile) {
			log.Warnf("Version file '%s' not found. "+
				"Automatic firmware update will not be enabled.", config.Flags.VersionFile)
		} else if !util.IsFileExists(config.Flags.ProvisioningScript) {
			log.Warnf("Provisioning script file '%s' not found. "+
				"Automatic firmware update will not be enabled.",
				config.Flags.ProvisioningScript)
		} else {
			wg.Add(1)
			go func() {
				defer wg.Done()
				autoUpdate.Start(channels.GetAutoUpdateChan())
			}()
		}
	}

	// Startup demo mode.
	if config.Flags.DemoInterval > 0 && mqttServerConnected {
		wg.Add(1)
		go func() {
			defer wg.Done()
			demoData.Start(channels.GetDemoChan())
		}()
	}

	log.Info("Device agent started.")
	for {
		time.Sleep(1000 * time.Millisecond)
	}
}
