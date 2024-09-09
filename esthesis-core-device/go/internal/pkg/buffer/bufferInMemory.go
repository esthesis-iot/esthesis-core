package buffer

import (
	"bytes"
	"container/list"
	"encoding/gob"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	log "github.com/sirupsen/logrus"
	"time"
)

type inMemoryBuffer struct {
	items   *list.List
	options Options
}

func (buffer inMemoryBuffer) Start(done chan bool) {

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

func (buffer inMemoryBuffer) Store(item Message) bool {
	sizeKB := buffer.SizeInKB()

	log.Debugf("buffer size is %dKB", sizeKB)

	if sizeKB > -1 && sizeKB <= buffer.options.SizeLimit {
		buffer.items.PushBack(&item)
		return true
	} else {
		log.Warnf("Buffer reached size limit and cant store new messages")
		return false
	}
}

func (buffer *inMemoryBuffer) SizeInKB() int {
	var buf bytes.Buffer
	enc := gob.NewEncoder(&buf)

	// Encode the entire list to the buffer
	err := enc.Encode(buffer.items)
	if err != nil {
		log.Warnf("Error encoding: %v", err)
		return -1
	}

	// Size in KB
	return int(buf.Len()) / 1024.0
}

func (buffer inMemoryBuffer) RetrieveNext() *list.Element {
	// Return nil if the buffer is empty
	if buffer.items.Len() == 0 {
		return nil
	}
	// Get the first element in the list
	element := buffer.items.Front()

	return element
}

func (buffer inMemoryBuffer) Remove(element *list.Element) bool {
	if element != nil {
		e := buffer.items.Remove(element)
		return e != nil
	} else {
		log.Debugf("Tried to remove a nil element from buffer")
	}

	return false

}

func NewInMemoryBuffer(options Options) Buffer {
	return &inMemoryBuffer{items: list.New(), options: options}
}

func (buffer inMemoryBuffer) Publish() {
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
