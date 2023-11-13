package mqttClient

import (
	"crypto/tls"
	"crypto/x509"
	"fmt"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttCommandRequestReceiver"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/util"
	log "github.com/sirupsen/logrus"
	"os"
	"time"
)

var client mqtt.Client

func getTlsConfig() *tls.Config {
	// Import CA.
	certPool := x509.NewCertPool()
	rootCa := config.GetRegistrationProperty(config.RegistrationPropertyRootCaCertificate)
	log.Debugf("Loaded CA certificate '%s'.", rootCa)
	certPool.AppendCertsFromPEM([]byte(rootCa))

	// Import client certificate and private key.
	clientCert := config.GetRegistrationProperty(config.RegistrationPropertyCertificate)
	log.Debugf("Loaded client certificate '%s'.", clientCert)
	clientKey := config.GetRegistrationProperty(config.RegistrationPropertyPrivateKey)
	log.Debugf("Loaded client private key '%s'.", clientKey)
	// cert, err := tls.LoadX509KeyPair(clientCert, clientKey)
	cert, err := tls.X509KeyPair([]byte(clientCert), []byte(clientKey))
	if err != nil {
		log.WithError(err).Error("Could not load X509 keypair.")
	}

	// Create tls.Config with desired tls properties
	return &tls.Config{
		RootCAs:            certPool,
		InsecureSkipVerify: config.Flags.IgnoreMqttInsecure,
		Certificates:       []tls.Certificate{cert},
	}
}

func Disconnect() {
	if client != nil {
		log.Debug("Unsubscribing from Command Request topic.")
		commandRequestTopic := config.Flags.
			TopicCommandRequest + "/" + config.Flags.HardwareId
		if token := client.Unsubscribe(commandRequestTopic); token.Wait() && token.
			Error() != nil {
			fmt.Println(token.Error())
			os.Exit(1)
		}

		log.Debug("Disconnecting from MQTT server.")
		client.Disconnect(250)
	}
}

func Connect() bool {
	mqttServer := config.GetRegistrationProperty(config.RegistrationPropertyMqttServer)
	// If no MQTT server is configured, do nothing.
	if mqttServer == "" {
		log.Warn("No MQTT server configured.")
		return false
	}

	log.Debugf("Connecting to MQTT server '%s'.", mqttServer)
	opts := mqtt.NewClientOptions()
	opts.AddBroker(mqttServer)
	opts.SetClientID(config.Flags.HardwareId)
	opts.SetKeepAlive(60 * time.Second)
	// Set TLS configuration, if MQTT broker URL starts with "ssl://".
	if mqttServer[0:6] == "ssl://" {
		tlsConfig := getTlsConfig()
		opts.SetTLSConfig(tlsConfig)
	}

	client = mqtt.NewClient(opts)
	token := client.Connect()
	if token.Wait() && token.Error() != nil {
		panic(token.Error())
	}

	// Subscribe to command request topic.
	commandRequestTopic := config.Flags.TopicCommandRequest + "/" + config.Flags.HardwareId
	log.Debugf("Subscribing to topic '%s'.", commandRequestTopic)
	if token := client.Subscribe(commandRequestTopic, 0,
		mqttCommandRequestReceiver.OnMessage); token.Wait() && token.Error() != nil {
		fmt.Println(token.Error())
		os.Exit(1)
	} else {
		log.Debugf("Subscribed to topic '%s'.", commandRequestTopic)
	}

	return true
}

func Publish(topic string, payload []byte) mqtt.Token {
	if client != nil {
		log.Debugf("Publishing '%s' to topic '%s'.", util.AbbrBA(payload), topic)
		return client.Publish(topic, 0, false, payload)
	} else {
		return nil
	}
}
