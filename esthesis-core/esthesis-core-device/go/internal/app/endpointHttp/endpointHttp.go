package endpointHttp

import (
	"context"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/luaExecutor"
	"github.com/julienschmidt/httprouter"
	log "github.com/sirupsen/logrus"
	"io"
	"net/http"
	"os"
	"strconv"
	"strings"
	"time"
)

func getBody(r *http.Request) []byte {
	body, err := io.ReadAll(r.Body)
	if err != nil {
		log.Errorf("Could not process request body due to '%s'.", err)
	}

	return body
}

func telemetryEndpoint(w http.ResponseWriter, r *http.Request, _ httprouter.Params) {
	// Get body.
	body := getBody(r)
	log.Debugf("Received telemetry data: '%s'.", body)

	// Check if payload should be transformed.
	if config.Flags.LuaHttpTelemetryScript != "" {
		body = []byte(luaExecutor.ExecuteLuaScript(string(body[:]),
			config.Flags.LuaHttpTelemetryScript))
	}

	// Send payload to MQTT broker.
	var topic = config.Flags.TopicTelemetry + "/" + config.Flags.HardwareId
	mqttClient.Publish(topic, body).WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)
}

func metadataEndpoint(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
	// Get body.
	body := getBody(r)
	log.Debugf("Received telemetry data: '%s'.", body)

	// Check if payload should be transformed.
	if config.Flags.LuaHttpMetadataScript != "" {
		body = []byte(luaExecutor.ExecuteLuaScript(string(body[:]),
			config.Flags.LuaHttpMetadataScript))
	}

	// Send payload to MQTT broker.
	var topic = config.Flags.TopicMetadata + "/" + config.Flags.HardwareId
	mqttClient.Publish(topic, body).WaitTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)
}

func startHTTPServer(r http.Handler, listeningAddress string) *http.Server {
	srv := &http.Server{
		Handler: r,
		Addr:    listeningAddress,
	}

	go func() {
		if err := srv.ListenAndServe(); err != nil {
			if strings.LastIndex(err.Error(), "Server closed") == -1 {
				log.Fatalf("Httpserver: ListenAndServe() error: %s", err)
			}
		}
	}()

	return srv
}

func Start(done chan bool) {
	httpListeningAddress := config.Flags.
		EndpointHttpListeningIP + ":" + strconv.Itoa(config.Flags.EndpointHttpListeningPort)

	router := httprouter.New()
	router.POST("/telemetry", telemetryEndpoint)
	router.POST("/metadata", metadataEndpoint)

	log.Infof("Starting embedded HTTP server at '%s'.",
		httpListeningAddress)
	srv := startHTTPServer(router, httpListeningAddress)

	<-done
	log.Debug("Stopping embedded HTTP server.")
	ctx, cancel := context.WithTimeout(context.Background(), time.Second*5)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		log.Error(err)
		os.Exit(config.ExitGeneric)
	}
}
