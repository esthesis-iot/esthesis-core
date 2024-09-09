package buffer

type Buffer interface {
	Store(item Message) bool
	Start(done chan bool)
}

type Options struct {
	SizeLimit       int // Buffer limit size in KB
	PublishInterval int // PublishNextMessage interval in MS
}

type Message struct {
	Timestamp int64 // Unix timestamp in nanoseconds to be used as the key
	Topic     string
	Payload   []byte
}
