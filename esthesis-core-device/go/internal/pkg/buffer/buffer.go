package buffer

type Buffer interface {
	Store(item Item) bool
	Start(done chan bool)
}

type BufferOptions struct {
	SizeLimit       int // Buffer limit size in KB
	PublishInterval int // Publish interval in MS
}

type Item struct {
	Timestamp int64 // Unix timestamp in nanoseconds to be used as the key
	Topic     string
	Payload   []byte
}
