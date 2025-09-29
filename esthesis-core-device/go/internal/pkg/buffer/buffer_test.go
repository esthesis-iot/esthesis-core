package buffer

import (
	"fmt"
	"math/rand"
	"os"
	"path/filepath"
	"testing"
	"time"

	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/testutils"
	"github.com/stretchr/testify/assert"
)

func TestInMemoryBuffer_BasicOperations(t *testing.T) {
	buf := NewInMemoryBuffer(Options{SizeLimit: 1024, PublishInterval: 1000})

	// Store a message.
	msg := Message{Timestamp: time.Now().UnixNano(), Topic: "test/topic", Payload: []byte("payload")}
	stored := buf.Store(msg)
	assert.True(t, stored, "Failed to store message in buffer.")

	// Retrieve and remove.
	memBuf, ok := buf.(*inMemoryBuffer)
	assert.True(t, ok, "Buffer is not inMemoryBuffer type.")
	elem := memBuf.RetrieveNext()
	assert.NotNil(t, elem, "No message retrieved from buffer.")
	removed := memBuf.Remove(elem)
	assert.True(t, removed, "Failed to remove message from buffer.")
}

func TestInMemoryBuffer_SizeLimit(t *testing.T) {
	// Store a message that reaches/exceeds the size limit.
	buf := NewInMemoryBuffer(Options{SizeLimit: 1, PublishInterval: 1000})                             // 1KB limit.
	msg := Message{Timestamp: time.Now().UnixNano(), Topic: "test/topic", Payload: make([]byte, 2048)} // 2KB payload.
	stored := buf.Store(msg)
	assert.True(t, stored, "Should store message while limit was not exceeded.")

	// Now the buffer is full, try to store another message.
	msg2 := Message{Timestamp: time.Now().UnixNano(), Topic: "test/topic", Payload: make([]byte, 1024)}
	stored2 := buf.Store(msg2)
	assert.False(t, stored2, "Should not store message because size limit was exceeded.")

}

func TestInMemoryBuffer_EmptyBuffer(t *testing.T) {
	buf := NewInMemoryBuffer(Options{SizeLimit: 1024, PublishInterval: 1000})
	memBuf := buf.(*inMemoryBuffer)
	elem := memBuf.RetrieveNext()
	assert.Nil(t, elem, "Expected nil for empty buffer.")
}

func TestOnDiskBuffer_BasicOperations(t *testing.T) {
	dbPath := filepath.Join(os.TempDir(), fmt.Sprintf("testdb_%d.db", rand.Int()))
	defer os.Remove(dbPath)
	buf := NewOnDiskBuffer(Options{SizeLimit: 1024, PublishInterval: 1000}, dbPath)

	// Store a message.
	msg := Message{Timestamp: time.Now().UnixNano(), Topic: "test/topic", Payload: []byte("payload")}
	stored := buf.Store(msg)
	assert.True(t, stored, "Failed to store message in disk buffer.")

	diskBuf, ok := buf.(*onDiskBuffer)
	assert.True(t, ok, "Buffer is not onDiskBuffer type.")

	// Retrieve and remove.
	retrieved := diskBuf.RetrieveNextMessage()
	assert.NotNil(t, retrieved, "No message retrieved from disk buffer.")
	removed := diskBuf.RemoveMessage(retrieved)
	assert.True(t, removed, "Failed to remove message from disk buffer.")
}

func TestOnDiskBuffer_SizeLimit(t *testing.T) {
	dbPath := filepath.Join(os.TempDir(), fmt.Sprintf("testdb_%d.db", rand.Int()))
	defer os.Remove(dbPath)
	buf := NewOnDiskBuffer(Options{SizeLimit: 32, PublishInterval: 1000}, dbPath)
	msg := Message{Timestamp: time.Now().UnixNano(), Topic: "test/topic", Payload: make([]byte, 1024*32)} // 32KB payload
	stored := buf.Store(msg)
	assert.True(t, stored, "Should store message while limit was not exceeded.")

	// Now the buffer is full, try to store another message.
	msg2 := Message{Timestamp: time.Now().UnixNano(), Topic: "test/topic", Payload: make([]byte, 1024)} // Another 1KB payload
	stored2 := buf.Store(msg2)
	assert.False(t, stored2, "Should not store message because size limit was exceeded.")
}

func TestOnDiskBuffer_EmptyBuffer(t *testing.T) {
	dbPath := filepath.Join(os.TempDir(), fmt.Sprintf("testdb_%d.db", rand.Int()))
	defer os.Remove(dbPath)
	buf := NewOnDiskBuffer(Options{SizeLimit: 1024, PublishInterval: 1000}, dbPath)
	diskBuf := buf.(*onDiskBuffer)
	retrieved := diskBuf.RetrieveNextMessage()
	assert.Nil(t, retrieved, "Expected nil for empty disk buffer.")
}

func TestInMemoryBuffer_Start(t *testing.T) {
	// Start a test MQTT broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize the MQTT client.
	mqttClient.Connect()

	// This test verifies that Start launches the publisher loop and removes messages when published.
	buf := NewInMemoryBuffer(Options{SizeLimit: 1024, PublishInterval: 100}) // Fast interval for test.
	memBuf := buf.(*inMemoryBuffer)
	msg := Message{Timestamp: time.Now().UnixNano(), Topic: "test/topic", Payload: []byte("payload")}
	memBuf.Store(msg)

	done := make(chan bool)
	go memBuf.Start(done)
	// Wait for at least one publish cycle.
	time.Sleep(200 * time.Millisecond)
	done <- true
	// Wait for goroutine to exit.
	time.Sleep(50 * time.Millisecond)
	assert.Equal(t, 0, memBuf.items.Len(), "Buffer should be empty after Start and publish.")

}

func TestOnDiskBuffer_Start(t *testing.T) {
	// Start a test MQTT broker.
	broker, brokerAddr := testutils.StartTestBroker(t)
	defer broker.Close()

	// Mock MQTT server address.
	testutils.MockProperties(t, map[string]string{
		config.RegistrationPropertyMqttServer: brokerAddr,
	})

	// Initialize the MQTT client.
	mqttClient.Connect()

	// This test verifies that Start launches the publisher loop and removes messages when published.
	dbPath := filepath.Join(os.TempDir(), fmt.Sprintf("testdb_%d.db", rand.Int()))
	defer os.Remove(dbPath)
	buf := NewOnDiskBuffer(Options{SizeLimit: 1024, PublishInterval: 100}, dbPath)
	diskBuf := buf.(*onDiskBuffer)
	msg := Message{Timestamp: time.Now().UnixNano(), Topic: "test/topic", Payload: []byte("payload")}
	diskBuf.Store(msg)

	done := make(chan bool)
	go diskBuf.Start(done)
	// Wait for at least one publish cycle.
	time.Sleep(200 * time.Millisecond)
	done <- true
	// Wait for goroutine to exit.
	time.Sleep(50 * time.Millisecond)
	assert.Equal(t, 0, diskBuf.getRowsInBucket(), "Disk buffer should be empty after Start and publish.")

}
