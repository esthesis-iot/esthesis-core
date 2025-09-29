package endpointHttp

import (
	"context"
	"io"
	"net/http"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/buffer"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/exitCodes"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/luaExecutor"
	"github.com/julienschmidt/httprouter"
	log "github.com/sirupsen/logrus"
)

func basicAuth(h func(w http.ResponseWriter, r *http.Request, _ httprouter.Params, buff buffer.Buffer), b buffer.Buffer) httprouter.Handle {
	return func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		requiredUser := config.Flags.EndpointHttpAuthUsername
		requiredPassword := config.Flags.EndpointHttpAuthPassword

		user, password, hasAuth := r.BasicAuth()

		if (requiredUser == "" || requiredPassword == "") || (hasAuth && user == requiredUser && password == requiredPassword) {
			h(w, r, ps, b)
		} else {
			// Request Basic Authentication
			w.Header().Set("WWW-Authenticate", "Basic realm=Restricted")
			http.Error(w, http.StatusText(http.StatusUnauthorized), http.StatusUnauthorized)
		}
	}
}

// Find the LUA handler for the provided custom telemetry endpoint name.
func getCustomTelemetryEndpointLuaHandler(endpointName string) string {
	for i := 0; i < len(config.Flags.LuaExtraHttpTelemetryEndpoint); i += 2 {
		if endpointName == config.Flags.LuaExtraHttpTelemetryEndpoint[i] {
			return config.Flags.LuaExtraHttpTelemetryEndpoint[i+1]
		}
	}
	return ""
}

// Find the LUA handler for the provided custom metadata endpoint name.
func getCustomMetadataEndpointLuaHandler(endpointName string) string {
	for i := 0; i < len(config.Flags.LuaExtraHttpMetadataEndpoint); i += 2 {
		if endpointName == config.Flags.LuaExtraHttpMetadataEndpoint[i] {
			return config.Flags.LuaExtraHttpMetadataEndpoint[i+1]
		}
	}
	return ""
}

func getBody(r *http.Request) []byte {
	body, err := io.ReadAll(r.Body)
	if err != nil {
		log.WithError(err).Errorf("Could not process request body.")
	}

	return body
}

func telemetryEndpoint(w http.ResponseWriter, r *http.Request, _ httprouter.Params, buff buffer.Buffer) {
	// Get body.
	body := getBody(r)
	uri := r.RequestURI
	log.Debugf("Received telemetry data: '%s' on URI: '%s'", body, uri)

	// Check if payload should be transformed.
	if config.Flags.LuaHttpTelemetryScript != "" {
		body = []byte(luaExecutor.ExecuteLuaScript(uri, string(body[:]),
			config.Flags.LuaHttpTelemetryScript))
	}

	// Send payload to MQTT broker.
	var topic = config.Flags.TopicTelemetry + "/" + config.Flags.HardwareId
	published := mqttClient.Publish(topic, body)

	// If publishing fails, store the message in the buffer to retry later.
	if !published {
		item := buffer.Message{Timestamp: time.Now().UnixNano(), Payload: body, Topic: topic}
		buff.Store(item)
	}
}

func metadataEndpoint(w http.ResponseWriter, r *http.Request, ps httprouter.Params, buff buffer.Buffer) {
	// Get body.
	body := getBody(r)
	uri := r.RequestURI
	log.Debugf("Received metadata data: '%s' on uri '%s'.", body, uri)

	// Check if payload should be transformed.
	if config.Flags.LuaHttpMetadataScript != "" {
		body = []byte(luaExecutor.ExecuteLuaScript(uri, string(body[:]),
			config.Flags.LuaHttpMetadataScript))
	}

	// Send payload to MQTT broker.
	var topic = config.Flags.TopicMetadata + "/" + config.Flags.HardwareId
	published := mqttClient.Publish(topic, body)

	// If publishing fails, store the message in the buffer to retry later.
	if !published {
		item := buffer.Message{Timestamp: time.Now().UnixNano(), Payload: body, Topic: topic}
		buff.Store(item)
	}
}

func customTelemetryEndpoint(w http.ResponseWriter, r *http.Request, _ httprouter.Params, buff buffer.Buffer) {
	// Get body.
	body := getBody(r)
	uri := r.RequestURI
	log.Debugf("Received telemetry data: '%s' on URI '%s'", body, uri)

	// Check if payload should be transformed.
	luaHandler := getCustomTelemetryEndpointLuaHandler(r.RequestURI)
	if luaHandler != "" {
		body = []byte(luaExecutor.ExecuteLuaScript(uri, string(body[:]), luaHandler))
	}

	// Send payload to MQTT broker.
	var topic = config.Flags.TopicTelemetry + "/" + config.Flags.HardwareId
	published := mqttClient.Publish(topic, body)

	// If publishing fails, store the message in the buffer to retry later.
	if !published {
		item := buffer.Message{Timestamp: time.Now().UnixNano(), Payload: body, Topic: topic}
		buff.Store(item)
	}
}

func customMetadataEndpoint(w http.ResponseWriter, r *http.Request, _ httprouter.Params, buff buffer.Buffer) {
	// Get body.
	body := getBody(r)
	uri := r.RequestURI
	log.Debugf("Received metadata data: '%s' on  URI '%s'", body, uri)

	// Check if payload should be transformed.
	luaHandler := getCustomMetadataEndpointLuaHandler(r.RequestURI)
	if luaHandler != "" {
		body = []byte(luaExecutor.ExecuteLuaScript(uri, string(body[:]), luaHandler))
	}

	// Send payload to MQTT broker.
	var topic = config.Flags.TopicMetadata + "/" + config.Flags.HardwareId
	published := mqttClient.Publish(topic, body)

	// If publishing fails, store the message in the buffer to retry later.
	if !published {
		item := buffer.Message{Timestamp: time.Now().UnixNano(), Payload: body, Topic: topic}
		buff.Store(item)
	}
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

func Start(done chan bool, buff buffer.Buffer) {
	// HTTP server listening address.
	httpListeningAddress := config.Flags.
		EndpointHttpListeningIP + ":" + strconv.Itoa(config.Flags.EndpointHttpListeningPort)

	// Default HTTP routes
	router := httprouter.New()
	router.POST("/telemetry", basicAuth(telemetryEndpoint, buff))
	router.POST("/metadata", basicAuth(metadataEndpoint, buff))

	// Create custom HTTP routes for LuaExtraHttpTelemetryEndpoint.
	for i := 0; i < len(config.Flags.LuaExtraHttpTelemetryEndpoint); i += 2 {
		router.POST(config.Flags.LuaExtraHttpTelemetryEndpoint[i], basicAuth(customTelemetryEndpoint, buff))
	}
	// Create custom HTTP routes for LuaExtraHttpMetadataEndpoint.
	for i := 0; i < len(config.Flags.LuaExtraHttpMetadataEndpoint); i += 2 {
		router.POST(config.Flags.LuaExtraHttpMetadataEndpoint[i], basicAuth(customMetadataEndpoint, buff))
	}

	log.Infof("Starting embedded HTTP server at '%s'.",
		httpListeningAddress)

	if config.Flags.EndpointHttpAuthUsername == "" || config.Flags.EndpointHttpAuthPassword == "" {
		log.Warnf("No basic auth defined for HTTP Endpoints!")
	} else {
		log.Debugf("Basic auth defined for HTTP Endpoints!"+
			" Only authenticated user '%s' will be allowed", config.Flags.EndpointHttpAuthUsername)
	}
	srv := startHTTPServer(router, httpListeningAddress)

	<-done
	log.Debug("Stopping embedded HTTP server.")
	ctx, cancel := context.WithTimeout(context.Background(), time.Second*5)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		log.WithError(err).Error("Could not shutdown embedded HTTP server.")
		os.Exit(exitCodes.ExitGeneric)
	}
}
