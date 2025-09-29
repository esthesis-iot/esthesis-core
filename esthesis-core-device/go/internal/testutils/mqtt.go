package testutils

import (
	"fmt"
	"net"
	"testing"
	"time"

	"github.com/eclipse/paho.mqtt.golang"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/mochi-co/mqtt/server"
	"github.com/mochi-co/mqtt/server/listeners"
	log "github.com/sirupsen/logrus"
)

// StartTestBroker Initializes a local MQTT broker on a random port.
// Returns the broker instance and its address (e.g., "tcp://127.0.0.1:12345").
func StartTestBroker(t *testing.T) (*server.Server, string) {
	t.Helper()

	config.Flags.MqttTimeout = 60

	// Create broker
	broker := server.NewServer(nil)

	// Get a random available port first.
	listener, err := net.Listen("tcp", ":0")
	if err != nil {
		t.Fatalf("Failed to find available port: %v", err)
	}
	port := listener.Addr().(*net.TCPAddr).Port
	// Close this temporary listener.
	listener.Close()

	// Create MQTT listener the known port.
	addr := fmt.Sprintf(":%d", port)
	tcpListener := listeners.NewTCP("tcp", addr)
	err = broker.AddListener(tcpListener, nil)
	if err != nil {
		t.Fatalf("Failed to start test broker: %v", err)
	}

	// Start broker.
	go func() {
		err := broker.Serve()
		if err != nil {
			t.Logf("Test broker stopped: %v", err)
		}
	}()

	// Wait until the TCP listener actually accepts connections.
	brokerAddr := fmt.Sprintf("127.0.0.1:%d", port)
	deadline := time.Now().Add(3 * time.Second)
	for {
		conn, err := net.DialTimeout("tcp", brokerAddr, 200*time.Millisecond)
		if err == nil {
			_ = conn.Close()
			break
		}
		if time.Now().After(deadline) {
			t.Fatalf("Broker did not become ready in time: %v", err)
		}
		time.Sleep(50 * time.Millisecond)
	}

	fullBrokerAddr := "tcp://" + brokerAddr

	log.Infof("Test broker running at %s", fullBrokerAddr)
	return broker, fullBrokerAddr
}

// GetMessages retrieves all published messages from all topics in the broker.
func GetMessages(broker *server.Server) map[int][]byte {
	allMessages := make(map[int][]byte)
	for index, messages := range broker.Topics.Messages("") {
		for _, msg := range messages.Payload {
			allMessages[index] = append(allMessages[index], msg)
		}
	}
	return allMessages
}

// CreateMqttClient initializes a new MQTT client for testing.
func CreateMqttClient(t *testing.T, brokerAddr string, username string, password string) mqtt.Client {
	t.Helper()

	opts := mqtt.NewClientOptions()
	opts.AddBroker(brokerAddr)
	opts.SetClientID("test-client-id")
	opts.SetCleanSession(true)

	// Set username and password for authentication
	if username != "" && password != "" {
		opts.SetUsername(username)
		opts.SetPassword(password)
	}

	client := mqtt.NewClient(opts)
	token := client.Connect()
	if !token.WaitTimeout(5*time.Second) || token.Error() != nil {
		t.Fatalf("Failed to connect to MQTT broker: %v", token.Error())
	}

	return client
}

// GetMockMessage creates a mock MQTT message with the specified topic and payload.
func GetMockMessage(topic string, payload []byte) *MockMessage {
	return &MockMessage{
		TopicName:   topic,
		PayloadName: payload,
	}
}

// MockMessage represents a mock implementation of mqtt.Message for testing purposes.
type MockMessage struct {
	TopicName   string // Renamed from Topic to TopicName
	PayloadName []byte
}

func (m *MockMessage) Ack() {
	// No-op for mock
}

func (m *MockMessage) Duplicate() bool {
	return false
}

func (m *MockMessage) Qos() byte {
	return 0
}

func (m *MockMessage) Retained() bool {
	return false
}

func (m *MockMessage) Topic() string {
	return m.TopicName // Updated to return TopicName
}

func (m *MockMessage) MessageID() uint16 {
	return 0
}

func (m *MockMessage) Payload() []byte {
	return m.PayloadName
}
