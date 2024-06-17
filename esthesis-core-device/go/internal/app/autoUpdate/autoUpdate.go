package autoUpdate

import (
	"crypto/tls"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/cryptoUtil"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/dto"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/util"
	"github.com/go-resty/resty/v2"
	log "github.com/sirupsen/logrus"
	"math/rand"
	"net/url"
	"os/exec"
	"path"
	"strconv"
	"time"
)

var versionFile string
var notBefore time.Time
var updateInProgress bool

func terminateUpdate() {
	updateInProgress = false
}

func IsUpdateInProgress() bool {
	return updateInProgress
}

func Update(packageId string) {
	// Find where provisioning information should be requested from.
	provisioningUrl := config.GetRegistrationProperty(config.RegistrationPropertyProvisioningUrl)

	// If a firmware update is already in progress, skip this request.
	if IsUpdateInProgress() {
		log.Debugf("Update already in progress. Skipping.")
		return
	}

	// If a firmware update handling script is not present, skip this request.
	if !util.IsFileExists(config.Flags.ProvisioningScript) {
		log.Warnf("Firmware update handling script '%s' not found, skipping firmware update.",
			config.Flags.ProvisioningScript)
		return
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
		log.Error("Current firmware version is unknown, aborting firmware upgrade.")
		terminateUpdate()
		return
	}
	log.Debugf("Current firmware version is '%s'.", currentVersion)

	// If provisioning requires signed tokens produce one.
	var token = ""
	if config.Flags.SecureProvisioning {
		var signature, err = cryptoUtil.Sign(
			config.GetRegistrationProperty(config.RegistrationPropertyPrivateKey),
			config.Flags.HardwareId)
		if err != nil {
			log.WithError(err).Errorf("Could not sign hardware ID.")
			terminateUpdate()
			return
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
	var url *url.URL
	if packageId == "" {
		finalProvisioningUrl := provisioningUrl + "/api/v1/provisioning/find"
		log.Debugf("Using provisioning URL '%s'", finalProvisioningUrl)
		url, err = url.Parse(finalProvisioningUrl)
	} else {
		finalProvisioningUrl := provisioningUrl + "/api/v1/provisioning/find/by-id"
		log.Debugf("Using provisioning URL '%s'", finalProvisioningUrl)
		url, err = url.Parse(finalProvisioningUrl)
	}
	if err != nil {
		log.WithError(err).Errorf("Could not parse provisioning URL '%s'.", provisioningUrl)
		terminateUpdate()
	}

	values := url.Query()
	if token != "" {
		values.Add("token", token)
	}
	values.Add("hardwareId", config.Flags.HardwareId)

	if packageId == "" {
		values.Add("version", currentVersion)
	} else {
		values.Add("packageId", packageId)
	}

	url.RawQuery = values.Encode()
	log.Debugf("Requesting provisioning packages from '%s'.", url.String())
	client := resty.New()
	client.SetTLSClientConfig(&tls.Config{InsecureSkipVerify: !config.Flags.TlsVerification})
	response, err := client.R().
		SetResult(&agentProvisioningInfoResponse).
		Get(url.String())
	if err != nil {
		log.WithError(err).Errorf("Could not get provisioning info.")
		terminateUpdate()
		return
	}
	if response.IsError() {
		log.Errorf("Could not get provisioning info due to '%s'.", response.Status())
		terminateUpdate()
		return
	}
	if agentProvisioningInfoResponse == nil || agentProvisioningInfoResponse.Id == "" {
		log.Debugf("No provisioning info available.")
		terminateUpdate()
		return
	}

	// Fetch provisioning package.
	log.Debugf("Downloading provisioning package '%+v'", agentProvisioningInfoResponse)
	downloadFilename := path.Join(config.Flags.TempDir, agentProvisioningInfoResponse.Filename)
	response, err = client.R().
		SetOutput(downloadFilename).
		SetQueryParam("token", agentProvisioningInfoResponse.DownloadToken).
		Get(provisioningUrl + "/api/v1/provisioning/download")
	if err != nil {
		log.WithError(err).Errorf("Could not download provisioning package.")
		terminateUpdate()
		return
	}
	if response.IsError() {
		log.Errorf("Could not download provisioning package due to '%s'.", response.Status())
		terminateUpdate()
		return
	}

	log.Debugf("Provisioning package downloaded to '%s'.", downloadFilename)

	// If a hash was received for this file, check it matches.
	if agentProvisioningInfoResponse.Sha256 != "" {
		encoded, err := cryptoUtil.HashFileEncoded(downloadFilename)
		if err != nil {
			log.WithError(err).Errorf("Could not hash downloaded file.")
			terminateUpdate()
			return
		}
		if encoded != agentProvisioningInfoResponse.Sha256 {
			log.Errorf("Hash of downloaded file does not match, calculated '%s' but expected '%s'.",
				encoded, agentProvisioningInfoResponse.Sha256)
			terminateUpdate()
			return
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
	}
	log.Info("Firmware update initiated.")
	terminateUpdate()
}

func Start(done chan bool) {
	versionFile = config.Flags.VersionFile
	log.Debug("Starting automatic firmware update monitoring.")

	// Wait for a random number of minutes (up to an hour) before start checking for firmware updates.
	// This is to distribute firmware update checks when multiple devices are rebooted together.
	notBefore = time.Now().Add(time.Duration(rand.Intn(60)) * time.Minute)
	log.Debugf("Will start checking for provisioning packages after %s.",
		notBefore.Format(time.RFC3339))

	// Check for new updates once per day.
	ticker := time.NewTicker(24 * time.Hour)
	defer func() { ticker.Stop() }()
	Update("")

LOOP:
	for {
		select {
		case doneMsg := <-done:
			if doneMsg {
				break LOOP
			} else {
				Update("")
			}
		case <-ticker.C:
			if time.Now().After(notBefore) {
				Update("")
			}
		}
	}

	log.Debugf("Automatic firmware update monitoring stopped.")
}
