package dto

import (
	"github.com/esthesis-iot/esthesis-device/internal/pkg/appConstants"
	"time"
)

type RegistrationRequest struct {
	HardwareId         string `json:"hardwareId"`
	Tags               string `json:"tags"`
	Attributes         string `json:"attributes"`
	Type               string `json:"type"`
	RegistrationSecret string `json:"registrationSecret"`
}

type RegistrationResponse struct {
	Certificate       string `json:"certificate"`
	PublicKey         string `json:"publicKey"`
	PrivateKey        string `json:"privateKey"`
	MqttServer        string `json:"mqttServer"`
	ProvisioningUrl   string `json:"provisioningUrl"`
	RootCaCertificate string `json:"rootCaCertificate"`
}

type ErrorReply struct {
	ErrorMessage string `json:"errorMessage"`
	TraceId      string `json:"traceId"`
}

type CommandRequest struct {
	Id            string
	Command       string
	Arguments     []string
	ExecutionType appConstants.CommandExecutionType
	CommandType   appConstants.CommandType
	CreatedOn     time.Time
}

type CommandReply struct {
	CorrelationId string
	Success       bool
	Output        string
}

type AgentProvisioningInfoResponse struct {
	Id            string                        `json:"id"`
	Name          string                        `json:"name"`
	Version       string                        `json:"version"`
	DownloadUrl   string                        `json:"downloadUrl"` // Set only for EXTERNAL type.
	Type          appConstants.ProvisioningType `json:"type"`
	Size          int64                         `json:"size"` // Set only for INTERNAL type.
	Sha256        string                        `json:"sha256"`
	DownloadToken string                        `json:"downloadToken"` // Set only for INTERNAL type.
}
