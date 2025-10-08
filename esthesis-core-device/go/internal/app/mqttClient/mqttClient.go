package mqttClient

import (
	"crypto/tls"
	"crypto/x509"
	"fmt"
	"os"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttCommandRequestReceiver"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/util"
	log "github.com/sirupsen/logrus"
)

var client mqtt.Client
var clientOptions *mqtt.ClientOptions

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

	log.Infof("Connecting to MQTT server '%s'.", mqttServer)
	opts := mqtt.NewClientOptions()
	opts.AddBroker(mqttServer)
	opts.SetClientID(config.Flags.HardwareId)
	opts.SetAutoReconnect(false)
	opts.SetConnectRetry(false)
	opts.SetKeepAlive(time.Duration(config.Flags.MqttTimeout) * time.Second)
	opts.SetWriteTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)
	opts.SetConnectTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)
	opts.SetPingTimeout(time.Duration(config.Flags.MqttTimeout) * time.Second)
	// Set TLS configuration, if MQTT broker URL starts with "ssl://".
	if mqttServer[0:6] == "ssl://" {
		tlsConfig := getTlsConfig()
		opts.SetTLSConfig(tlsConfig)
	}

	opts.SetConnectionLostHandler(func(c mqtt.Client, err error) {
		go autoReconnectClient()
	})

	opts.SetOnConnectHandler(func(c mqtt.Client) {
		// Subscribe to command request topic.
		commandRequestTopic := config.Flags.TopicCommandRequest + "/" + config.Flags.HardwareId
		log.Debugf("Subscribing to topic '%s'.", commandRequestTopic)
		if token := c.Subscribe(commandRequestTopic, 0,
			mqttCommandRequestReceiver.OnMessage); token.Wait() && token.Error() != nil {
			log.Debugf("Error Subscribing to topic '%s'. Error '%s'", commandRequestTopic, token.Error())
		} else {
			log.Debugf("Subscribed to topic '%s'.", commandRequestTopic)
		}
	})

	client = mqtt.NewClient(opts)
	clientOptions = opts
	token := client.Connect()
	if token.Wait() && token.Error() != nil {
		log.Warnf("Error Connecting to MQTT server. Error '%s'", token.Error())
		return false
	}

	return true
}

func Publish(topic string, payload []byte) bool {
	if client != nil {
		log.Debugf("Publishing '%s' to topic '%s'.", util.AbbrBA(payload), topic)
		token := client.Publish(topic, 0, false, payload)
		if token.Wait() && token.Error() != nil {
			log.Warnf("Failed publishing '%s' to topic '%s' due error : '%v'.", util.AbbrBA(payload), topic, token.Error())
			return false
		}
		return true
	} else {
		log.Warn("Failed publishing to MQTT, client is nil.")
		return false
	}
}

func autoReconnectClient() {
	log.Debugf("Trying to reconnect MQTT client")
	for {
		// Ensure at least 1-second delay before each retry.
		time.Sleep(1 * time.Second)
		client = mqtt.NewClient(clientOptions)
		token := client.Connect()
		if token.Wait() && token.Error() == nil {
			log.Debugf("MQTT Client reconnected successfully")
			return
		}
		log.Debugf("Unable to reconnect MQTT Client error: %v", token.Error())
	}
}
