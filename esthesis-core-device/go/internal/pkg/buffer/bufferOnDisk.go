package buffer

import (
	"bytes"
	"encoding/gob"
	"errors"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	log "github.com/sirupsen/logrus"
	"go.etcd.io/bbolt"
	"strconv"
	"time"
)

const (
	BucketName = "MqttMessages"
)

type onDiskBuffer struct {
	db      *bbolt.DB
	options BufferOptions
}

func NewOnDiskBuffer(options BufferOptions, dbPath string) Buffer {
	db, err := bbolt.Open(dbPath, 0600, nil)
	if err != nil {
		log.Fatalf("Error creating on disk buffer: %v", err)
		return nil
	}

	err = db.Update(func(tx *bbolt.Tx) error {
		b, err := tx.CreateBucketIfNotExists([]byte(BucketName))
		if err != nil {
			return err
		}
		if b != nil {
			log.Debugf("Bucket '%v' status: ", b.Stats())
		}

		return nil
	})

	if err != nil {
		log.Debugf("Error creating bucket '%v'", err)
	}

	return &onDiskBuffer{
		db:      db,
		options: options,
	}
}

func (buffer *onDiskBuffer) Start(done chan bool) {
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

func (buffer *onDiskBuffer) Store(item Item) bool {
	log.Debugf("Storing item %d", item.Timestamp)
	sizeKB := buffer.SizeInKB()

	if sizeKB > -1 && sizeKB <= buffer.options.SizeLimit {
		err := buffer.db.Update(func(tx *bbolt.Tx) error {
			b, err := tx.CreateBucketIfNotExists([]byte(BucketName))
			if err != nil {
				return err
			}

			// generate key from timestamp in bbolt expected format
			key := []byte(strconv.FormatInt(item.Timestamp, 10))

			// Generate the value from item in bbolt expected format
			var buf bytes.Buffer
			enc := gob.NewEncoder(&buf)
			if err := enc.Encode(item); err != nil {
				log.Fatalf("failed to encode: %v", err)
			}

			value := buf.Bytes()

			return b.Put(key, value)
		})
		if err != nil {
			log.Errorf("Error storing item: %v", err)
			return false
		}
		return true
	} else {
		log.Warnf("Buffer reached size limit and can't store new items")
		return false
	}
}

func (buffer *onDiskBuffer) SizeInKB() int {
	var size int
	err := buffer.db.View(func(tx *bbolt.Tx) error {
		size = int(tx.Size())
		return nil
	})

	if err != nil {
		log.Errorf("Error getting buffer size: '%v' ", err)
		return -1
	}

	sizeKB := (size / 1024)
	log.Debugf("cache size is %dKB", sizeKB)
	return sizeKB
}

func (buffer *onDiskBuffer) RetrieveNext() *Item {
	var item Item
	err := buffer.db.View(func(tx *bbolt.Tx) error {
		b := tx.Bucket([]byte(BucketName))
		if b != nil {
			c := b.Cursor()
			k, v := c.First()
			if k != nil {
				dec := gob.NewDecoder(bytes.NewReader(v))
				err := dec.Decode(&item)
				if err != nil {
					return err
				}
				return nil
			} else {
				return errors.New("buffer is empty")
			}
		} else {
			// It is expected the bucket to be nil until the first
			return errors.New("bucket is nil")
		}
	})
	if err != nil {
		//log.Debugf("Unable to retrieve next cached element: '%s'", err)
		return nil
	}

	if item.Timestamp == 0 {
		log.Debug("no next item was found")
		return nil
	}

	return &item
}

func (buffer *onDiskBuffer) Remove(item *Item) bool {
	log.Debugf("Removing item %d", item.Timestamp)
	err := buffer.db.Update(func(tx *bbolt.Tx) error {
		b := tx.Bucket([]byte(BucketName))
		if b != nil {
			// generate key from timestamp in bbolt expected format
			key := []byte(strconv.FormatInt(item.Timestamp, 10))
			return b.Delete(key)
		}
		return nil
	})
	if err != nil {
		log.Errorf("Error removing item: %v", err)
		return false
	}
	log.Debugf("Removed item %d with success", item.Timestamp)
	return true
}

func (buffer *onDiskBuffer) Publish() {
	item := buffer.RetrieveNext()
	if item != nil {
		topic := item.Topic
		payload := item.Payload
		published := mqttClient.Publish(topic, payload)
		// If published with success then remove the item from buffer
		if published {
			buffer.Remove(item)
		}
	}
}
