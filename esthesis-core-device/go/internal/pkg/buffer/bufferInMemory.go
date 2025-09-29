package buffer

import (
	"bytes"
	"container/list"
	"encoding/gob"
	"strings"
	"time"

	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	log "github.com/sirupsen/logrus"
)

type inMemoryBuffer struct {
	items   *list.List
	options Options
}

func (buffer *inMemoryBuffer) Start(done chan bool) {

	ticker := time.NewTicker(time.Duration(buffer.options.PublishInterval) * time.Millisecond)
	defer func() { ticker.Stop() }()
	buffer.Publish()
LOOP:
	for {
		select {
		case doneMsg := <-done:
			if doneMsg {
				break LOOP
			} else {
				buffer.Publish()
			}
		case <-ticker.C:
			buffer.Publish()
		}
	}
	log.Debug("Buffer MQTT Publisher stopped.")
}

func (buffer *inMemoryBuffer) Store(item Message) bool {
	payloadStr := string(item.Payload)
	payloadStr = strings.TrimSpace(payloadStr)
	tokens := strings.Split(payloadStr, " ")
	if len(tokens) < 3 {
		// If no timestamp present, append current UTC timestamp.
		ts := time.Now().UTC().Format("2006-01-02T15:04:05Z")
		payloadStr = payloadStr + " " + ts
		item.Payload = []byte(payloadStr)
	}

	sizeKB := buffer.SizeInKB()
	if sizeKB > -1 && sizeKB <= buffer.options.SizeLimit {
		buffer.items.PushBack(&item)
		log.Debugf("New Message stored in Memory Buffer. It now has %d items and size of %dKB.", buffer.items.Len(), buffer.SizeInKB())
		return true
	} else {
		log.Warnf("Buffer reached size limit of %dKB with %d items. ", buffer.options.SizeLimit, buffer.items.Len())
		return false
	}
}

func (buffer *inMemoryBuffer) SizeInKB() int {
	var buf bytes.Buffer
	enc := gob.NewEncoder(&buf)

	// Collect all messages into a slice.
	var messages []Message
	for e := buffer.items.Front(); e != nil; e = e.Next() {
		msg, ok := e.Value.(*Message)
		if ok {
			messages = append(messages, *msg)
		}
	}

	// Encode the slice of messages.
	err := enc.Encode(messages)
	if err != nil {
		log.Warnf("Error encoding: %v", err)
		return -1
	}

	// Size in KB.
	return int(buf.Len()) / 1024
}

func (buffer *inMemoryBuffer) RetrieveNext() *list.Element {
	// Return nil if the buffer is empty
	if buffer.items.Len() == 0 {
		return nil
	}
	// Get the first element in the list
	element := buffer.items.Front()

	return element
}

func (buffer *inMemoryBuffer) Remove(element *list.Element) bool {
	if element != nil {
		e := buffer.items.Remove(element)
		log.Debugf("Message removed from memory Buffer. It now has %d items and size of %dKB.", buffer.items.Len(), buffer.SizeInKB())
		return e != nil
	} else {
		log.Debugf("Tried to remove a nil element from memory buffer")
	}

	return false

}

func NewInMemoryBuffer(options Options) Buffer {
	return &inMemoryBuffer{items: list.New(), options: options}
}

func (buffer *inMemoryBuffer) Publish() {
	element := buffer.RetrieveNext()
	if element != nil {
		item := element.Value.(*Message)
		topic := item.Topic
		payload := item.Payload
		published := mqttClient.Publish(topic, payload)
		// If published with success then remove the element from buffer
		if published {
			buffer.Remove(element)
		}
	}
}
