package endpointMqtt

import log "github.com/sirupsen/logrus"

type Auth struct {
	Users map[string]string
}

func (a *Auth) Authenticate(user, password []byte) bool {

	// if auth is disable
	if a.Users == nil || len(a.Users) == 0 {
		return true
	}

	// if auth is enabled and user/pass are correct
	if pass, ok := a.Users[string(user)]; ok && pass == string(password) {
		return true
	}

	// if auth is enabled and user or pass is incorrect
	return false
}

// ACL returns true if a user has access permissions to read or write on a topic.
func (a *Auth) ACL(user []byte, topic string, write bool) bool {
	return true
}

// Init allowed user map, if empty means to allow unauthenticated connections
func createAuth(user string, password string) *Auth {
	if user == "" || password == "" {
		log.Warnf("No authentication defined for the MQTT Endpoint!")
		return &Auth{Users: map[string]string{}}
	}

	log.Debugf("Authentication defined for the MQTT Endpoint! Only authenticated user '%s' will be allowed", user)
	return &Auth{Users: map[string]string{user: password}}
}
