package autoUpdate

import (
	"crypto/tls"
	"errors"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/appConstants"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/cryptoUtil"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/dto"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/util"
	"github.com/go-resty/resty/v2"
	"github.com/google/uuid"
	log "github.com/sirupsen/logrus"
	"math/rand"
	"net/url"
	"os/exec"
	"path"
	"strconv"
	"time"
)

var notBefore time.Time
var updateInProgress bool

const provisioningContextRoot = "/api/v1/provisioning"

func terminateUpdate() {
	updateInProgress = false
	log.Debug("Terminated firmware update.")
}

func IsUpdateInProgress() bool {
	return updateInProgress
}

func Update(packageId string) (string, error) {
	log.Debug("Starting firmware update.")
	var errMessage string
	// Find where provisioning information should be requested from. If no provisιoning URL is set,
	// skip this request.
	provisioningUrl := config.GetRegistrationProperty(config.RegistrationPropertyProvisioningUrl)
	if provisioningUrl == "" {
		errMessage = "No provisioning URL set, skipping firmware update."
		log.Debugf(errMessage)
		return "", errors.New(errMessage)
	}

	// If a firmware update is already in progress, skip this request.
	if IsUpdateInProgress() {
		errMessage = "Update already in progress. Skipping."
		log.Debugf(errMessage)
		return "", errors.New(errMessage)
	}

	// If a firmware update handling script is not present, skip this request.
	if !util.IsFileExists(config.Flags.ProvisioningScript) {
		log.Warnf("Firmware update handling script '%s' not found, skipping firmware update.",
			config.Flags.ProvisioningScript)
		return "", errors.New("update handling script not found")
	}

	// Proceed with the update.
	updateInProgress = true
	if packageId == "" {
		log.Debugf("Checking for firmware updates at '%s'.", provisioningUrl)
	} else {
		log.Debugf("Updating firmware to provisioning package id '%s'.", packageId)
	}

	// Read the version of the currently installed firmware.
	currentVersion := util.GetFirmwareVersion()
	if currentVersion == "" {
		errMessage = "Current firmware version is unknown, aborting firmware upgrade."
		log.Error(errMessage)
		terminateUpdate()
		return "", errors.New(errMessage)
	}
	log.Debugf("Current firmware version is '%s'.", currentVersion)

	// If provisioning requires a signed token produce one.
	var token = ""
	if config.Flags.SecureProvisioning {
		var signature, err = cryptoUtil.Sign(
			config.GetRegistrationProperty(config.RegistrationPropertyPrivateKey),
			config.Flags.HardwareId)
		if err != nil {
			log.WithError(err).Errorf("Could not sign hardware ID.")
			terminateUpdate()
			return "", errors.New(err.Error())
		} else {
			log.Debugf("Signature: %s", signature)
			token = signature
		}
	} else {
		log.Debugf("Provisioning is setup to not require a security token.")
	}

	// Find available provisioning packages or get the specific provisioning package requested.
	var agentProvisioningInfoResponse *dto.AgentProvisioningInfoResponse
	var err error
	var provisioningPackageUrl *url.URL
	if packageId == "" {
		finalProvisioningUrl := provisioningUrl + provisioningContextRoot + "/find"
		log.Debugf("Using provisioning URL '%s'", finalProvisioningUrl)
		provisioningPackageUrl, err = provisioningPackageUrl.Parse(finalProvisioningUrl)
	} else {
		finalProvisioningUrl := provisioningUrl + provisioningContextRoot + "/find/by-id"
		log.Debugf("Using provisioning URL '%s'", finalProvisioningUrl)
		provisioningPackageUrl, err = provisioningPackageUrl.Parse(finalProvisioningUrl)
	}
	if err != nil {
		errMessage = "Could not parse provisioning URL '" + provisioningUrl + "'"
		log.WithError(err).Errorf(errMessage)
		terminateUpdate()
		return "", errors.New(errMessage + " " + err.Error())
	}

	values := provisioningPackageUrl.Query()
	if token != "" {
		values.Add("token", token)
	}
	values.Add("hardwareId", config.Flags.HardwareId)

	if packageId == "" {
		values.Add("version", currentVersion)
	} else {
		values.Add("packageId", packageId)
	}

	provisioningPackageUrl.RawQuery = values.Encode()
	log.Debugf("Requesting provisioning packages from '%s'.", provisioningPackageUrl.String())
	client := resty.New()
	client.SetTLSClientConfig(&tls.Config{InsecureSkipVerify: !config.Flags.TlsVerification})
	response, err := client.R().
		SetResult(&agentProvisioningInfoResponse).
		Get(provisioningPackageUrl.String())
	if err != nil {
		log.WithError(err).Errorf("Could not get provisioning info.")
		terminateUpdate()
		return "", errors.New(err.Error())
	}
	if response.IsError() {
		errMessage = "Could not get provisioning info due to '" + response.Status() + "'."
		terminateUpdate()
		return "", errors.New(errMessage + " " + response.String())
	}
	if agentProvisioningInfoResponse == nil || agentProvisioningInfoResponse.Id == "" {
		errMessage = "No provisioning info available for provisioning package with id '" + packageId + "'."
		log.Debugf(errMessage)
		terminateUpdate()
		return "", errors.New(errMessage)
	}

	// Fetch provisioning package.
	downloadFilename := path.Join(config.Flags.TempDir, uuid.New().String())
	log.Debugf("Downloading '%+v' provisioning package '%+v' to '%+v'.",
		agentProvisioningInfoResponse.Type, agentProvisioningInfoResponse, downloadFilename)
	var downloadResponse *resty.Response
	var downloadErr error
	if agentProvisioningInfoResponse.Type == appConstants.ProvisioningPackageTypeInternal {
		downloadResponse, downloadErr = client.R().
			SetOutput(downloadFilename).
			SetQueryParam("token", agentProvisioningInfoResponse.DownloadToken).
			Get(provisioningUrl + provisioningContextRoot + "/download")
	} else if agentProvisioningInfoResponse.Type == appConstants.ProvisioningPackageTypeExternal {
		downloadResponse, downloadErr = client.R().
			SetOutput(downloadFilename).
			Get(agentProvisioningInfoResponse.DownloadUrl)
	} else {
		errMessage = string("Unknown provisioning type '" + agentProvisioningInfoResponse.Type + "'.")
		log.Error(errMessage)
		terminateUpdate()
		return "", errors.New(errMessage)
	}
	if downloadErr != nil {
		log.WithError(err).Errorf("Could not download provisioning package.")
		terminateUpdate()
		return "", errors.New(err.Error())
	}
	if downloadResponse.IsError() {
		log.Errorf("Could not download provisioning package due to '%s'.", response.Status())
		terminateUpdate()
		return "", errors.New(response.String())
	}
	log.Debugf("Provisioning package downloaded to '%s'.", downloadFilename)

	// If a hash was received for this file, check it matches.
	if agentProvisioningInfoResponse.Sha256 != "" {
		encoded, err := cryptoUtil.HashFileEncoded(downloadFilename)
		if err != nil {
			log.WithError(err).Errorf("Could not hash downloaded file.")
			terminateUpdate()
			return "", errors.New(err.Error())
		}
		if encoded != agentProvisioningInfoResponse.Sha256 {
			log.Errorf("Hash of downloaded file does not match, calculated '%s' but expected '%s'.",
				encoded, agentProvisioningInfoResponse.Sha256)
			terminateUpdate()
			return "", errors.New("hash of downloaded file does not match")
		} else {
			log.Debugf("Hash of downloaded file matches, '%s'.", encoded)
		}
	} else {
		log.Debugf("No hash received for downloaded file.")
	}

	// Execute the firmware update script.
	// The following parameters are passed to the script:
	// 1. The current version of the firmware.
	// 2. The new version of the downloaded firmware.
	// 3. The SHA256 hash of the downloaded firmware or "0" if no hash was received.
	// 4. The size in bytes of the provisioning file (as reported from the esthesis platform).
	// 5. The full path and filename to the downloaded provisioning package.
	log.Infof("Initiating firmware update with script '%s' for firmware '%s'",
		config.Flags.ProvisioningScript, downloadFilename)
	cmd := exec.Command(config.Flags.ProvisioningScript)
	cmd.Args = append(cmd.Args, currentVersion)
	cmd.Args = append(cmd.Args, agentProvisioningInfoResponse.Version)
	if agentProvisioningInfoResponse.Sha256 != "" {
		cmd.Args = append(cmd.Args, agentProvisioningInfoResponse.Sha256)
	} else {
		cmd.Args = append(cmd.Args, "0")
	}
	cmd.Args = append(cmd.Args, strconv.FormatInt(agentProvisioningInfoResponse.Size, 10))
	cmd.Args = append(cmd.Args, downloadFilename)
	err = cmd.Start()
	if err != nil {
		log.WithError(err).Errorf("Could not execute firmware update.")
		return "", errors.New(err.Error())
	}
	infoMessage := "Firmware update initiated."
	log.Info(infoMessage)
	terminateUpdate()
	return infoMessage, nil
}

func Start(done chan bool) {
	log.Debug("Starting automatic firmware update monitoring.")

	// Wait for a random number of minutes (up to an hour) before start checking for firmware updates.
	// This is to distribute firmware update checks when multiple devices are rebooted together.
	notBefore = time.Now().Add(time.Duration(rand.Intn(60)) * time.Minute)
	log.Debugf("Will start checking for provisioning packages after %s.",
		notBefore.Format(time.RFC3339))

	// Check for new updates once per day.
	ticker := time.NewTicker(24 * time.Hour)
	defer func() { ticker.Stop() }()
	_, err := Update("")
	if err != nil {
		log.WithError(err).Errorf("Could not check for firmware updates.") // NOSONAR
	}

LOOP:
	for {
		select {
		case doneMsg := <-done:
			if doneMsg {
				break LOOP
			} else {
				_, err := Update("")
				if err != nil {
					log.WithError(err).Errorf("Could not check for firmware updates.")
				}
			}
		case <-ticker.C:
			if time.Now().After(notBefore) {
				_, err := Update("")
				if err != nil {
					log.WithError(err).Errorf("Could not check for firmware updates.")
				}
			}
		}
	}

	log.Debugf("Automatic firmware update monitoring stopped.")
}
