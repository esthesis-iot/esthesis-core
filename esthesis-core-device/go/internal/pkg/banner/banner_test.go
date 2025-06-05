package banner

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestPrint(t *testing.T) {
	assert.NotPanics(t, func() {
		Print("test-version")
	})
}
