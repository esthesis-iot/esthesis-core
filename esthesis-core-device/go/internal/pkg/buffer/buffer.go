package buffer

type Buffer interface {
	Store(item Message) bool
	Start(done chan bool)
}

type Options struct {
	SizeLimit       int // Buffer size limit in KB.
	PublishInterval int // Publish interval in seconds.
}

type Message struct {
	Timestamp int64  // Unix timestamp in nanoseconds to store when the message was received.
	Topic     string // Topic to publish the message to.
	Payload   []byte
}
