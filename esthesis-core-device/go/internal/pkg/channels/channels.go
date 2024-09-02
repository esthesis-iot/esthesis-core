package channels

import (
	"sync"
)

var endpointHttpChan chan bool
var endpointMqttChan chan bool
var pingChan chan bool
var demoChan chan bool
var healthChan chan bool
var autoUpdateChan chan bool
var lock sync.Mutex
var isShutdown = false
var mqttPublishChan chan bool

func GetPingChan() chan bool {
	lock.Lock()
	if pingChan == nil {
		pingChan = make(chan bool)
	}
	lock.Unlock()
	return pingChan
}

func GetDemoChan() chan bool {
	lock.Lock()
	if demoChan == nil {
		demoChan = make(chan bool)
	}
	lock.Unlock()
	return demoChan
}

func GetEndpointHttpChan() chan bool {
	lock.Lock()
	if endpointHttpChan == nil {
		endpointHttpChan = make(chan bool)
	}
	lock.Unlock()
	return endpointHttpChan
}

func GetEndpointMqttChan() chan bool {
	lock.Lock()
	if endpointMqttChan == nil {
		endpointMqttChan = make(chan bool)
	}
	lock.Unlock()

	return endpointMqttChan
}

func GetHealthChan() chan bool {
	lock.Lock()
	if healthChan == nil {
		healthChan = make(chan bool)
	}
	lock.Unlock()

	return healthChan
}

func GetAutoUpdateChan() chan bool {
	lock.Lock()
	if autoUpdateChan == nil {
		autoUpdateChan = make(chan bool)
	}
	lock.Unlock()

	return autoUpdateChan
}

func GetIsShutdown() bool {
	return isShutdown
}

func GetMqttPublishChan() chan bool {
	lock.Lock()
	if mqttPublishChan == nil {
		mqttPublishChan = make(chan bool)
	}
	lock.Unlock()

	return mqttPublishChan
}

func Shutdown() {
	isShutdown = true
}

func IsAutoUpdateChan() bool {
	return autoUpdateChan != nil
}

func IsEndpointHttpChan() bool {
	return endpointHttpChan != nil
}

func IsEndpointMqttChan() bool {
	return endpointMqttChan != nil
}

func IsPingChan() bool {
	return pingChan != nil
}

func IsDemoChan() bool {
	return demoChan != nil
}

func IsHealthChan() bool {
	return healthChan != nil
}

func IsMqttPublishChan() bool { return mqttPublishChan != nil }
