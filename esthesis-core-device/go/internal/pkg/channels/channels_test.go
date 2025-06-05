package channels

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestPingChan(t *testing.T) {
	assert.False(t, IsPingChan())
	GetPingChan()
	assert.True(t, IsPingChan())
}

func TestDemoChan(t *testing.T) {
	assert.False(t, IsDemoChan())
	GetDemoChan()
	assert.True(t, IsDemoChan())
}

func TestEndpointHttpChan(t *testing.T) {
	assert.False(t, IsEndpointHttpChan())
	GetEndpointHttpChan()
	assert.True(t, IsEndpointHttpChan())
}

func TestEndpointMqttChan(t *testing.T) {
	assert.False(t, IsEndpointMqttChan())
	GetEndpointMqttChan()
	assert.True(t, IsEndpointMqttChan())
}

func TestHealthChan(t *testing.T) {
	assert.False(t, IsHealthChan())
	GetHealthChan()
	assert.True(t, IsHealthChan())
}

func TestAutoUpdateChan(t *testing.T) {
	assert.False(t, IsAutoUpdateChan())
	GetAutoUpdateChan()
	assert.True(t, IsAutoUpdateChan())
}

func TestShutdown(t *testing.T) {
	assert.False(t, GetIsShutdown())
	Shutdown()
	assert.True(t, GetIsShutdown())
}
