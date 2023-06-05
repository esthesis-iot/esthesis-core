package config

import (
	"fmt"
	"github.com/DavidGamba/go-getoptions"
	"github.com/magiconair/properties"
	log "github.com/sirupsen/logrus"
	"os"
	"path/filepath"
)

// Version The application version
const Version = "v3.0.0-SNAPSHOT"

const ExitGeneric = 1
const ExitCodeCouldNotRegister = 2
const ExitCodeCouldNotLoadRegistrationProperties = 3
const ExitCodeRegistrationInterrupted = 4
const ExitHelp = 5
const ExitVersion = 6
const ExitCliParse = 7
const ExitUnsupportedSignatureAlgorithm = 8

// Properties received when this device was registered with the esthesis platform.
const (
	RegistrationPropertyCertificate       = "Certificate"
	RegistrationPropertyPublicKey         = "PublicKey"
	RegistrationPropertyMqttServer        = "MqttServer"
	RegistrationPropertyProvisioningUrl   = "ProvisioningUrl"
	RegistrationPropertyRootCaCertificate = "RootCaCertificate"
	RegistrationPropertyPrivateKey        = "PrivateKey"
)

// FlagsStruct represents the command line flags.
type flagsStruct struct {
	RegistrationURL           string
	HardwareId                string
	PauseStartup              bool
	PropertiesFile            string
	SecurePropertiesFile      string
	TempDir                   string
	HttpTimeout               int // in seconds
	MqttTimeout               int // in seconds
	RetryHttpRequest          int // in seconds
	TopicPing                 string
	TopicTelemetry            string
	TopicMetadata             string
	TopicCommandRequest       string
	TopicCommandReply         string
	HealthReportInterval      int // in seconds
	PingInterval              int // in seconds
	LogLevel                  string
	LogAbbreviation           int // abbreviate log messages to this length
	Tags                      string
	EndpointHttp              bool
	EndpointHttpListeningIP   string
	EndpointHttpListeningPort int
	EndpointMqtt              bool
	EndpointMqttListeningIP   string
	EndpointMqttListeningPort int
	AutoUpdate                bool
	SecureProvisioning        bool
	SignatureAlgorithm        string
	VersionFile               string
	VersionReport             bool
	VersionReportTopic        string
	ProvisioningScript        string
	RebootScript              string
	ShutdownScript            string
	SupportedCommands         string
	TopicDemo                 string
	DemoCategory              string
	DemoInterval              int // in seconds
	RegistrationSecret        string
	Attributes                string
}

var Flags = flagsStruct{}
var registrationProperties *properties.Properties

// Finds the home directory for the current user.
func getHomeDir() string {
	var homeDir, err = os.UserHomeDir()
	if err != nil {
		log.Fatal(err)
	}

	return homeDir
}

// InitCmdFlags reads CLI parameters while providing default values.
func InitCmdFlags(osArgs []string) {
	opt := getoptions.New()
	opt.SetUnknownMode(getoptions.Warn)
	opt.Self("esthesis-device", "The esthesis device agent, "+
		"allowing a device to connect to the esthesis platform")
	opt.HelpSynopsisArgs(" ")
	opt.Bool("help", false, opt.Alias("h", "?"), opt.Description("Show this help"))
	opt.Bool("version", false, opt.Alias("v"), opt.Description("Show the version"))

	// Mandatory flags
	opt.StringVar(&Flags.RegistrationURL, "registrationUrl", "",
		opt.Required(),
		opt.Description("The URL of esthesis server to register this device with"))
	opt.StringVar(&Flags.HardwareId, "hardwareId", "",
		opt.Required(),
		opt.Description("The hardware ID this device will present to esthesis server"))

	// Optional flags
	opt.IntVar(&Flags.HttpTimeout, "httpTimeout", 60,
		opt.Description("The number of seconds after which an HTTP call times out"))
	opt.StringVar(&Flags.PropertiesFile, "propertiesFile",
		filepath.Join(getHomeDir(), ".esthesis", "device", "esthesis.properties"),
		opt.Description("The file to store the agent’s configuration"))
	opt.StringVar(&Flags.SecurePropertiesFile, "securePropertiesFile",
		filepath.Join(getHomeDir(), ".esthesis", "device", "secure", "esthesis.properties"),
		opt.Description("The secure file to store sensitive parts of the agent's configuration"))
	opt.StringVar(&Flags.TempDir, "tempDir",
		filepath.Join(os.TempDir()),
		opt.Description("The folder to temporarily store provisioning packages"))
	opt.BoolVar(&Flags.PauseStartup, "pauseStartup", false,
		opt.Description("A flag indicating whether the device should start paused"))
	opt.IntVar(&Flags.RetryHttpRequest, "httpRetry", 60,
		opt.Description("The number of seconds to wait before retrying a failed HTTP request"))
	opt.StringVar(&Flags.TopicPing, "topicPing", "esthesis/ping",
		opt.Description("The topic to use for ping messages"))
	opt.StringVar(&Flags.TopicTelemetry, "topicTelemetry",
		"esthesis/telemetry", opt.Description("The topic to use for telemetry messages"))
	opt.StringVar(&Flags.TopicMetadata, "topicMetadata", "esthesis/metadata",
		opt.Description("The topic to use for metadata messages"))
	opt.StringVar(&Flags.TopicCommandRequest, "topicCommandRequest",
		"esthesis/command/request",
		opt.Description("The topic to use for command request messages"))
	opt.StringVar(&Flags.TopicCommandReply, "topicCommandReply",
		"esthesis/command/reply",
		opt.Description("The topic to use for command reply messages"))
	opt.IntVar(&Flags.HealthReportInterval, "healthReportInterval", 300,
		opt.Description("The frequency in which to send health reports ("+
			"in seconds)"))
	opt.IntVar(&Flags.PingInterval, "pingInterval", 60,
		opt.Description("The frequency in which to ping back the esthesis server("+
			"in seconds)"))
	opt.StringVar(&Flags.LogLevel, "logLevel",
		"info", opt.Description("The logging level to use [trace, debug, info]"))
	opt.IntVar(&Flags.LogAbbreviation, "logAbbreviation",
		1024, opt.Description("The length to abbreviate log messages to"))
	opt.StringVar(&Flags.Tags, "tags",
		"", opt.Description("A comma-separated list of tags to associate with this device"))
	opt.BoolVar(&Flags.EndpointHttp, "endpointHttp",
		false, opt.Description("Whether the embedded HTTP server is enabled"))
	opt.StringVar(&Flags.EndpointHttpListeningIP, "endpointHttpListeningIP",
		"127.0.0.1", opt.Description("The IP address where the embedded HTTP server listens to"))
	opt.IntVar(&Flags.EndpointHttpListeningPort, "endpointHttpListeningPort",
		8080, opt.Description("The port in which the embedded HTTP server listens to"))
	opt.IntVar(&Flags.MqttTimeout, "mqttTimeout}",
		60, opt.Description("The number of seconds to wait before failing an outgoing MQTT"+
			" message"))
	opt.BoolVar(&Flags.EndpointMqtt, "endpointMqtt",
		false, opt.Description("Whether the embedded MQTT server is enabled"))
	opt.StringVar(&Flags.EndpointMqttListeningIP, "endpointMqttListeningIP",
		"127.0.0.1", opt.Description("The IP address where the embedded MQTT server listens to"))
	opt.IntVar(&Flags.EndpointMqttListeningPort, "endpointMqttListeningPort}",
		1883, opt.Description("The port in which the embedded MQTT server listens to"))
	opt.BoolVar(&Flags.AutoUpdate, "autoUpdate", false,
		opt.Description("A flag indicating whether the device should try to automatically obtain newer firmware"+
			" once per day"))
	opt.BoolVar(&Flags.SecureProvisioning, "secureProvisioning", false,
		opt.Description("A flag indicating whether provisioning requests should be accompanied by a signature"+
			" token"))
	opt.StringVar(&Flags.SignatureAlgorithm, "signatureAlgorithm", "SHA256WITHRSA",
		opt.Description("The algorithm to use to produce signatures"))
	opt.StringVar(&Flags.VersionFile, "versionFile",
		filepath.Join(getHomeDir(), ".esthesis", "device", "version"),
		opt.Description("A file with a single line of text depicting the current version of the firmware"+
			" running on the device"))
	opt.BoolVar(&Flags.VersionReport, "versionReport", false,
		opt.Description("Report the version number available in the specified version file"+
			" once during boot"))
	opt.StringVar(&Flags.VersionReportTopic, "versionReportTopic", "esthesis/metadata",
		opt.Description("The MQTT topic to report the firmware version"))
	opt.StringVar(&Flags.ProvisioningScript, "provisioningScript",
		filepath.Join(getHomeDir(), ".esthesis", "device", "firmware.sh"),
		opt.Description("The script used to install new provisioning packages"))
	opt.StringVar(&Flags.RebootScript, "rebootScript",
		filepath.Join(getHomeDir(), ".esthesis", "device", "reboot.sh"),
		opt.Description("The script used to reboot the device"))
	opt.StringVar(&Flags.ShutdownScript, "shutdownScript",
		filepath.Join(getHomeDir(), ".esthesis", "device", "shutdown.sh"),
		opt.Description("The script used to shutdown the device"))
	opt.StringVar(&Flags.SupportedCommands, "supportedCommands", "efrsph",
		opt.Description("The remote commands this device supports:\n"+
			"e: Execute arbitrary\n"+
			"f: Firmware update\n"+
			"r: Reboot\n"+
			"s: Shutdown\n"+
			"p: Ping\n"+
			"h: Health report"))
	opt.StringVar(&Flags.TopicDemo, "topicDemo", "esthesis/telemetry",
		opt.Description("The MQTT topic to post demo data"))
	opt.StringVar(&Flags.DemoCategory, "demoCategory", "demo",
		opt.Description("The category of data posted as demo data"))
	opt.IntVar(&Flags.DemoInterval, "demoInterval", 0,
		opt.Description("The frequency in which demo data is generated, in seconds"))
	opt.StringVar(&Flags.RegistrationSecret, "registrationSecret", "",
		opt.Description("If set, the registration request will include it as a header"))
	opt.StringVar(&Flags.Attributes, "attributes", "",
		opt.Description("A comma-separated list of key-value pairs to be sent as attributes"))

	// Parse CLI arguments.
	_, err := opt.Parse(osArgs)
	if opt.Called("help") {
		fmt.Fprintf(os.Stderr, opt.Help())
		os.Exit(ExitHelp)
	}
	if opt.Called("version") {
		fmt.Println(Version)
		os.Exit(ExitVersion)
	}
	if err != nil {
		fmt.Fprintf(os.Stderr, "ERROR: %s\n\n", err)
		fmt.Fprintf(os.Stderr, opt.Help(getoptions.HelpSynopsis))
		os.Exit(ExitCliParse)
	}
	// Currently, only SHA256WITHRSA is supported.
	if Flags.SignatureAlgorithm != "SHA256WITHRSA" {
		log.Error("Only SHA256WITHRSA is supported for signatureAlgorithm.")
		os.Exit(ExitUnsupportedSignatureAlgorithm)
	}
}

// InitRegistrationProperties reads the properties provided during registration.
func InitRegistrationProperties() {
	var err error
	registrationProperties, err = properties.LoadAll(
		[]string{Flags.PropertiesFile, Flags.SecurePropertiesFile}, properties.UTF8, false)
	if err != nil {
		log.Errorf("Could not load agent registration properties due to '%s'.",
			err)
		os.Exit(ExitCodeCouldNotLoadRegistrationProperties)
	}
}

func GetRegistrationProperty(propertyName string) string {
	prop, _ := registrationProperties.Get(propertyName)
	return prop
}
