package buffer

import (
	"bytes"
	"encoding/gob"
	"errors"
	"github.com/esthesis-iot/esthesis-device/internal/app/mqttClient"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	log "github.com/sirupsen/logrus"
	"go.etcd.io/bbolt"
	"os"
	"strconv"
	"time"
)

const (
	BucketName = "MqttMessages"
)

type onDiskBuffer struct {
	db      *bbolt.DB
	options Options
}

func NewOnDiskBuffer(options Options, dbPath string) Buffer {
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
	buffer.PublishNextMessage()
LOOP:
	for {
		select {
		case doneMsg := <-done:
			if doneMsg {
				break LOOP
			} else {
				buffer.PublishNextMessage()
			}
		case <-ticker.C:
			buffer.PublishNextMessage()

			cacheSizeKB := buffer.getCacheSizeKB()
			rows := buffer.getRowsInBucket()

			//  free disk space if DB is empty and cache size is equal or bigger than the threshold value established,
			if rows == 0 && cacheSizeKB >= config.Flags.BufferFreeSpaceThreshold {
				log.Infof("Cache size (%vKB) is has reached the threshold (%vKB)", cacheSizeKB, config.Flags.BufferFreeSpaceThreshold)
				buffer.freeDBDiskSpace()
				log.Infof("CacheSize: %vKB  Rows: %v", cacheSizeKB, rows)
			} else if rows > 0 {
				log.Debugf("CacheSize: %vKB  Rows: %v", cacheSizeKB, rows)
			}

		}
	}
	log.Debug("Buffer MQTT Publisher stopped.")
}

func (buffer *onDiskBuffer) Store(message Message) bool {
	log.Debugf("Storing message %d", message.Timestamp)
	sizeKB := buffer.getCacheSizeKB()

	if sizeKB > -1 && sizeKB <= buffer.options.SizeLimit {
		err := buffer.db.Update(func(tx *bbolt.Tx) error {
			b, err := tx.CreateBucketIfNotExists([]byte(BucketName))
			if err != nil {
				return err
			}

			// Generate a key from the message's timestamp, formatted as a string for bbolt.
			key := []byte(strconv.FormatInt(message.Timestamp, 10))

			// Generate the value from message in bbolt expected format
			var buf bytes.Buffer
			enc := gob.NewEncoder(&buf)
			if err := enc.Encode(message); err != nil {
				log.Fatalf("failed to encode: %v", err)
			}

			value := buf.Bytes()

			return b.Put(key, value)
		})
		if err != nil {
			log.Errorf("Error storing message: %v", err)
			return false
		}
		return true
	} else {
		log.Warnf("Buffer reached size limit and can't store new messages")
		return false
	}
}

func (buffer *onDiskBuffer) getCacheSizeKB() int {
	var size int
	err := buffer.db.View(func(tx *bbolt.Tx) error {
		size = int(tx.Size())
		return nil
	})

	if err != nil {
		log.Errorf("Error getting buffer size: '%v' ", err)
		return -1
	}

	sizeKB := size / 1024
	return sizeKB
}

func (buffer *onDiskBuffer) getRowsInBucket() int {
	rows := -1
	buffer.db.View(func(tx *bbolt.Tx) error {
		b := tx.Bucket([]byte(BucketName))
		if b != nil {
			rows = b.Stats().KeyN
		}
		return nil
	})

	return rows
}

func (buffer *onDiskBuffer) RetrieveNextMessage() *Message {
	var message Message
	err := buffer.db.View(func(tx *bbolt.Tx) error {
		b := tx.Bucket([]byte(BucketName))
		if b != nil {
			c := b.Cursor()
			k, v := c.First()
			if k != nil {
				dec := gob.NewDecoder(bytes.NewReader(v))
				err := dec.Decode(&message)
				if err != nil {
					return err
				}
				return nil
			} else {
				return errors.New("buffer is empty")
			}
		} else {
			// It is expected for the bucket to be nil until the first message is stored in the buffer.
			return errors.New("bucket is nil")
		}
	})
	if err != nil {
		//log.Debugf("Unable to retrieve next cached element: '%s'", err)
		return nil
	}

	if message.Timestamp == 0 {
		log.Debug("no next message was found")
		return nil
	}

	return &message
}

func (buffer *onDiskBuffer) RemoveMessage(message *Message) bool {
	log.Debugf("Removing message %d", message.Timestamp)
	err := buffer.db.Update(func(tx *bbolt.Tx) error {
		b := tx.Bucket([]byte(BucketName))
		if b != nil {
			// generate key from timestamp in bbolt expected format
			key := []byte(strconv.FormatInt(message.Timestamp, 10))
			return b.Delete(key)
		}
		return nil
	})
	if err != nil {
		log.Errorf("Error removing message: %v", err)
		return false
	}
	return true
}

func (buffer *onDiskBuffer) PublishNextMessage() bool {
	message := buffer.RetrieveNextMessage()
	if message != nil {
		topic := message.Topic
		payload := message.Payload
		published := mqttClient.Publish(topic, payload)
		// If published with success then remove the message from buffer
		if published {
			buffer.RemoveMessage(message)
		}
		return published
	}
	return false
}

// Create a copy of the current database and apply the compact command to reclaim the free space
// The compacted DB shall become the current DB
func (buffer *onDiskBuffer) freeDBDiskSpace() {
	log.Info("Free DB Disk space procediment started! ")
	// Prepare auxiliary paths to hold temp files
	currentDBPath := buffer.db.Path()
	tempDBPath := currentDBPath + ".temp"
	deleteDBPath := currentDBPath + ".remove"

	// Remove any previous temp just in case they exist
	os.Remove(deleteDBPath)
	os.Remove(tempDBPath)

	// Initialize a temporary database, which will be the destination for the compact operation.
	tempDB, err := bbolt.Open(tempDBPath, 0600, nil)
	if err != nil {
		log.Errorf("Error during DB Compaction while openning new DB: %v", err)
		return
	}
	defer tempDB.Close()

	// Apply the compact function which will create a compacted file with only non-removed data
	err = bbolt.Compact(tempDB, buffer.db, 32000)
	if err != nil {
		log.Errorf("Error during DB Compaction while compacting DB: %v", err)
		tempDB.Close()
		return
	}

	// Closes both opened DBs to unlock its files
	tempDB.Close()
	buffer.db.Close()

	// Rename current DB to another name so it can be deleted safely
	err = os.Rename(currentDBPath, deleteDBPath)
	if err != nil {
		log.Errorf("Error during DB Compaction while removing old DB: %v", err)
		tempDB.Close()
		return
	}

	// Rename compacted DB file to be the current valid one
	err = os.Rename(tempDBPath, currentDBPath)
	if err != nil {
		log.Errorf("Error during DB Compaction while renaming new DB: %v", err)
		tempDB.Close()
		return
	}

	// Re-open current DB which now will use the compacted file
	db, err := bbolt.Open(currentDBPath, 0600, nil)
	if err != nil {
		log.Fatalf("Error during DB Compaction while re-openning current DB: %v", err)
		return
	}
	buffer.db = db

	// remove old file
	os.Remove(deleteDBPath)
	log.Info("DB disk space was freed successfully!")
}
