package registration

import (
	"context"
	"crypto/tls"
	"encoding/json"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/appConstants"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/channels"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/dto"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/exitCodes"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/util"
	"github.com/go-resty/resty/v2"
	"github.com/magiconair/properties"
	log "github.com/sirupsen/logrus"
	"math"
	"os"
	"path/filepath"
	"time"
)

func Register() bool {
	// Check if the device is already registered.
	propertiesFileLocation := config.Flags.PropertiesFile
	log.Debugf("Looking for insecure properties file at '%s'.", propertiesFileLocation)
	securePropertiesFileLocation := config.Flags.SecurePropertiesFile
	log.Debugf("Looking for secure properties file at '%s'.", securePropertiesFileLocation)
	if util.IsFileExists(propertiesFileLocation) && util.IsFileExists(
		securePropertiesFileLocation) {
		log.Debug("Device is already registered. Skipping registration.")
		return false
	}

	// Register the device.
	var registerResponse *dto.RegistrationResponse
	log.Infof("Attempting device registration at '%s', using hardware id '%s'.",
		config.Flags.RegistrationURL, config.Flags.HardwareId)
	client := resty.New()
	client.SetRetryCount(math.MaxInt)
	client.SetRetryWaitTime(time.Duration(config.Flags.RetryHttpRequest) * time.Second)
	client.SetTimeout(time.Duration(config.Flags.HttpTimeout) * time.Second)
	client.SetTLSClientConfig(&tls.Config{InsecureSkipVerify: !config.Flags.TlsVerification})
	client.AddRetryCondition(
		func(r *resty.Response, err error) bool {
			if r.StatusCode() > 200 && !channels.GetIsShutdown() {
				var errorReply *dto.ErrorReply
				err := json.Unmarshal(r.Body(), &errorReply)
				if err == nil {
					log.WithFields(log.Fields{"traceId": errorReply.TraceId}).
						Warnf("Registration failed with '%s'. Retrying after '%d' seconds.",
							r.Status(), config.Flags.RetryHttpRequest)
				} else {
					log.Warnf("Registration failed with '%s'. Retrying after '%d' seconds.",
						r.Status(), config.Flags.RetryHttpRequest)
				}
				return true
			} else {
				return false
			}
		},
	)

	requestBody := dto.RegistrationRequest{
		HardwareId: config.Flags.HardwareId,
		Type:       appConstants.DeviceType,
		Attributes: config.Flags.Attributes,
		Tags:       config.Flags.Tags}
	if config.Flags.RegistrationSecret != "" {
		requestBody.RegistrationSecret = config.Flags.RegistrationSecret
	}
	if config.Flags.IgnoreHttpsInsecure {
		client.SetTLSClientConfig(&tls.Config{InsecureSkipVerify: true})
	}
	request := client.R().
		SetBody(requestBody).
		SetResult(&registerResponse).
		SetContext(context.Background())
	response, err := request.Post(config.Flags.RegistrationURL)
	// Check if registration was successful.
	if err != nil {
		log.WithError(err).Errorf("Unhandled error.")
		os.Exit(exitCodes.ExitCodeCouldNotRegister)
	} else if response.IsError() {
		log.Errorf("Could not register device due to '%s'.", response.Status())
		os.Exit(exitCodes.ExitCodeCouldNotRegister)
	} else if channels.GetIsShutdown() {
		log.Errorf("Client shutdown detected during registration.")
		os.Exit(exitCodes.ExitCodeRegistrationInterrupted)
	}

	// Handle a successful registration.
	log.Info("Registration successful, persisting registration data.")

	// Create non-secure properties file.
	esthesisProperties := properties.NewProperties()
	esthesisProperties.Set(config.RegistrationPropertyCertificate,
		registerResponse.Certificate)
	esthesisProperties.Set(config.RegistrationPropertyPublicKey,
		registerResponse.PublicKey)
	esthesisProperties.Set(config.RegistrationPropertyMqttServer,
		registerResponse.MqttServer)
	esthesisProperties.Set(config.RegistrationPropertyProvisioningUrl,
		registerResponse.ProvisioningUrl)
	esthesisProperties.Set(config.RegistrationPropertyRootCaCertificate,
		registerResponse.RootCaCertificate)

	log.Infof("Creating directory '%s' for insecure properties.",
		filepath.Dir(propertiesFileLocation))
	errHnd := os.MkdirAll(filepath.Dir(propertiesFileLocation), 0755)
	if errHnd != nil {
		log.WithError(errHnd).Errorf("Could not create directory %s for insecure"+
			" properties.", filepath.Dir(propertiesFileLocation))
		os.Exit(exitCodes.ExitCodeCouldNotRegister)
	}
	log.Infof("Creating file '%s' for insecure properties.",
		propertiesFileLocation)
	esthesisPropertiesFile, errHnd := os.Create(propertiesFileLocation)
	if errHnd != nil {
		log.WithError(errHnd).Errorf("Could not create file '%s'.", propertiesFileLocation)
		os.Exit(exitCodes.ExitCodeCouldNotRegister)
	}
	defer esthesisPropertiesFile.Close()
	_, errHnd = esthesisProperties.Write(esthesisPropertiesFile, properties.UTF8)
	if errHnd != nil {
		log.WithError(errHnd).Errorf("Could not write to file '%s'.")
		os.Exit(exitCodes.ExitCodeCouldNotRegister)
	}
	errHnd = esthesisPropertiesFile.Sync()
	if errHnd != nil {
		log.WithError(errHnd).Errorf("Could not sync file '%s'.", propertiesFileLocation)
		os.Exit(exitCodes.ExitCodeCouldNotRegister)
	}

	// Create secure properties file.
	esthesisSecureProperties := properties.NewProperties()
	esthesisSecureProperties.Set(config.RegistrationPropertyPrivateKey,
		registerResponse.PrivateKey)
	log.Infof("Creating directory '%s' for secure properties.",
		filepath.Dir(securePropertiesFileLocation))
	errHnd = os.MkdirAll(filepath.Dir(securePropertiesFileLocation), 0755)
	if errHnd != nil {
		log.WithError(errHnd).Errorf("Could not create directory '%s'.",
			filepath.Dir(securePropertiesFileLocation))
		os.Exit(exitCodes.ExitCodeCouldNotRegister)
	}
	log.Infof("Creating file '%s' for secure properties.",
		securePropertiesFileLocation)
	esthesisSecurePropertiesFile, errHnd := os.Create(securePropertiesFileLocation)
	if errHnd != nil {
		log.WithError(errHnd).Errorf("Could not create file '%s'.", securePropertiesFileLocation)
		os.Exit(exitCodes.ExitCodeCouldNotRegister)
	}
	defer esthesisSecurePropertiesFile.Close()
	_, errHnd = esthesisSecureProperties.Write(esthesisSecurePropertiesFile,
		properties.UTF8)
	if errHnd != nil {
		log.WithError(errHnd).Errorf("Could not write to file '%s'.", securePropertiesFileLocation)
		os.Exit(exitCodes.ExitCodeCouldNotRegister)
	}
	esthesisSecurePropertiesFile.Sync()

	// Check if an MQTT server was received, if not issue a warning.
	if registerResponse.MqttServer == "" {
		log.Warn("No MQTT server received.")
	}

	log.Info("Registration data persisted successfully.")

	return true
}
