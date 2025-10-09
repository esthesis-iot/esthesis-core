package testutils

import (
	"strings"
	"sync"

	log "github.com/sirupsen/logrus"
)

type LogHook struct {
	mu      sync.Mutex
	entries []*log.Entry
}

func (h *LogHook) Levels() []log.Level { return log.AllLevels }
func (h *LogHook) Fire(e *log.Entry) error {
	h.mu.Lock()
	defer h.mu.Unlock()
	// copy to decouple from later mutations
	c := *e
	h.entries = append(h.entries, &c)
	return nil
}

func (h *LogHook) Contains(substr string) bool {
	h.mu.Lock()
	defer h.mu.Unlock()
	for _, e := range h.entries {
		if strings.Contains(e.Message, substr) {
			return true
		}
	}
	return false
}

func NewLogHook() *LogHook {
	return &LogHook{}
}
