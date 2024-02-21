package config

import (
	"fmt"
	"github.com/DavidGamba/go-getoptions"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/exitCodes"
	"github.com/magiconair/properties"
	log "github.com/sirupsen/logrus"
	"os"
	"path/filepath"
)

// Version The application version
const Version = "3.0.11"

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
	RegistrationURL               string
	HardwareId                    string
	PauseStartup                  bool
	PropertiesFile                string
	SecurePropertiesFile          string
	TempDir                       string
	HttpTimeout                   int // in seconds
	MqttTimeout                   int // in seconds
	RetryHttpRequest              int // in seconds
	TopicPing                     string
	TopicTelemetry                string
	TopicMetadata                 string
	TopicCommandRequest           string
	TopicCommandReply             string
	HealthReportInterval          int // in seconds
	PingInterval                  int // in seconds
	LogLevel                      string
	LogAbbreviation               int // abbreviate log messages to this length
	Tags                          string
	EndpointHttp                  bool
	EndpointHttpListeningIP       string
	EndpointHttpListeningPort     int
	EndpointMqtt                  bool
	EndpointMqttListeningIP       string
	EndpointMqttListeningPort     int
	AutoUpdate                    bool
	SecureProvisioning            bool
	SignatureAlgorithm            string
	VersionFile                   string
	VersionReport                 bool
	VersionReportTopic            string
	ProvisioningScript            string
	RebootScript                  string
	ShutdownScript                string
	SupportedCommands             string
	TopicDemo                     string
	DemoCategory                  string
	DemoInterval                  int // in seconds
	RegistrationSecret            string
	Attributes                    string
	LuaHttpTelemetryScript        string
	LuaHttpMetadataScript         string
	LuaMqttTelemetryScript        string
	LuaMqttMetadataScript         string
	IgnoreHttpsInsecure           bool
	IgnoreMqttInsecure            bool
	LuaExtraMqttTelemetryTopic    []string
	LuaExtraMqttMetadataTopic     []string
	LuaExtraHttpTelemetryEndpoint []string
	LuaExtraHttpMetadataEndpoint  []string
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
	opt.Self("esthesis-core-device", "The esthesis device agent, "+
		"allowing a device to connect to the esthesis CORE platform")
	opt.Bool("help", false, opt.Alias("h", "?"), opt.Description("Show this help"))
	opt.Bool("version", false, opt.Alias("v"), opt.Description("Show the version"))

	// Mandatory flags
	opt.StringVar(&Flags.RegistrationURL, "registrationUrl", "",
		opt.GetEnv("REGISTRATION_URL"), opt.Required(),
		opt.Description("The URL of esthesis server to register this device with"))
	opt.StringVar(&Flags.HardwareId, "hardwareId", "",
		opt.GetEnv("HARDWARE_ID"), opt.Required(),
		opt.Description("The hardware ID this device will present to esthesis server"))

	// Optional flags
	opt.IntVar(&Flags.HttpTimeout, "httpTimeout", 60,
		opt.GetEnv("HTTP_TIMEOUT"),
		opt.Description("The number of seconds after which an HTTP call times out"))
	opt.StringVar(&Flags.PropertiesFile, "propertiesFile",
		filepath.Join(getHomeDir(), ".esthesis", "device", "esthesis.properties"),
		opt.GetEnv("PROPERTIES_FILE"),
		opt.Description("The file to store the agent’s configuration"))
	opt.StringVar(&Flags.SecurePropertiesFile, "securePropertiesFile",
		filepath.Join(getHomeDir(), ".esthesis", "device", "secure", "esthesis.properties"),
		opt.GetEnv("SECURE_PROPERTIES_FILE"),
		opt.Description("The secure file to store sensitive parts of the agent's configuration"))
	opt.StringVar(&Flags.TempDir, "tempDir",
		filepath.Join(os.TempDir()),
		opt.GetEnv("TEMP_DIR"),
		opt.Description("The folder to temporarily store provisioning packages"))
	opt.BoolVar(&Flags.PauseStartup, "pauseStartup", false,
		opt.GetEnv("PAUSE_STARTUP"),
		opt.Description("A flag indicating whether the device should start paused"))
	opt.IntVar(&Flags.RetryHttpRequest, "httpRetry", 60,
		opt.GetEnv("HTTP_RETRY"),
		opt.Description("The number of seconds to wait before retrying a failed HTTP request"))

	// MQTT topics
	opt.StringVar(&Flags.TopicPing, "topicPing", "esthesis/ping",
		opt.GetEnv("TOPIC_PING"),
		opt.Description("The topic to use to send ping messages"))
	opt.StringVar(&Flags.TopicTelemetry, "topicTelemetry", "esthesis/telemetry",
		opt.GetEnv("TOPIC_TELEMETRY"),
		opt.Description("The topic to use to send telemetry messages"))
	opt.StringVar(&Flags.TopicMetadata, "topicMetadata", "esthesis/metadata",
		opt.GetEnv("TOPIC_METADATA"),
		opt.Description("The topic to use to send metadata messages"))
	opt.StringVar(&Flags.TopicCommandRequest, "topicCommandRequest", "esthesis/command/request",
		opt.GetEnv("TOPIC_COMMAND_REQUEST"),
		opt.Description("The topic to use to receive command request messages"))
	opt.StringVar(&Flags.TopicCommandReply, "topicCommandReply", "esthesis/command/reply",
		opt.GetEnv("TOPIC_COMMAND_REPLY"),
		opt.Description("The topic to use for command reply messages"))

	// LUA scripts
	opt.StringVar(&Flags.LuaHttpTelemetryScript, "luaHttpTelemetryScript", "",
		opt.GetEnv("LUA_HTTP_TELEMETRY_SCRIPT"),
		opt.Description("The Lua script to transform telemetry messages for HTTP endpoint"))
	opt.StringVar(&Flags.LuaHttpMetadataScript, "luaHttpMetadataScript", "",
		opt.GetEnv("LUA_HTTP_METADATA_SCRIPT"),
		opt.Description("The Lua script to transform metadata messages for HTTP endpoint"))
	opt.StringVar(&Flags.LuaMqttTelemetryScript, "luaMqttTelemetryScript", "",
		opt.GetEnv("LUA_MQTT_TELEMETRY_SCRIPT"),
		opt.Description("The Lua script to transform telemetry messages for MQTT endpoint"))
	opt.StringVar(&Flags.LuaMqttMetadataScript, "luaMqttMetadataScript", "",
		opt.GetEnv("LUA_MQTT_METADATA_SCRIPT"),
		opt.Description("The Lua script to transform metadata messages for MQTT endpoint"))

	opt.StringSliceVar(&Flags.LuaExtraMqttTelemetryTopic, "luaExtraMqttTelemetryTopic", 2, 2,
		opt.Description("A custom MQTT telemetry topic (arg 1) to listen to, "+
			"being processed by a custom Lua script (arg 2)"))
	opt.StringSliceVar(&Flags.LuaExtraMqttMetadataTopic, "luaExtraMqttMetadataTopic", 2, 2,
		opt.Description("A custom MQTT metadata topic (arg 1) to listen to, "+
			"being processed by a custom Lua script (arg 2)"))
	opt.StringSliceVar(&Flags.LuaExtraHttpTelemetryEndpoint, "luaExtraHttpTelemetryEndpoint", 2, 2,
		opt.Description("A custom HTTP telemetry endpoint (arg 1) to listen to, "+
			"being processed by a custom Lua script (arg 2)"))
	opt.StringSliceVar(&Flags.LuaExtraHttpMetadataEndpoint, "luaExtraHttpMetadataEndpoint", 2, 2,
		opt.Description("A custom HTTP metadata endpoint (arg 1) to listen to, "+
			"being processed by a custom Lua script (arg 2)"))

	opt.IntVar(&Flags.HealthReportInterval, "healthReportInterval", 300,
		opt.GetEnv("HEALTH_REPORT_INTERVAL"),
		opt.Description("The frequency in which to send health reports ("+
			"in seconds)"))
	opt.IntVar(&Flags.PingInterval, "pingInterval", 60,
		opt.GetEnv("PING_INTERVAL"),
		opt.Description("The frequency in which to ping back the esthesis server("+
			"in seconds)"))
	opt.StringVar(&Flags.LogLevel, "logLevel",
		"info", opt.GetEnv("LOG_LEVEL"),
		opt.Description("The logging level to use [trace, debug, info]"))
	opt.IntVar(&Flags.LogAbbreviation, "logAbbreviation",
		1024, opt.GetEnv("LOG_ABBREVIATION"),
		opt.Description("The length to abbreviate log messages to"))
	opt.StringVar(&Flags.Tags, "tags",
		"", opt.GetEnv("TAGS"),
		opt.Description("A comma-separated list of tags to associate with this device"))
	opt.BoolVar(&Flags.EndpointHttp, "endpointHttp",
		false, opt.GetEnv("ENDPOINT_HTTP"),
		opt.Description("Whether the embedded HTTP server is enabled"))
	opt.StringVar(&Flags.EndpointHttpListeningIP, "endpointHttpListeningIP",
		"127.0.0.1", opt.GetEnv("ENDPOINT_HTTP_LISTENING_IP"),
		opt.Description("The IP address where the embedded HTTP server listens to"))
	opt.IntVar(&Flags.EndpointHttpListeningPort, "endpointHttpListeningPort",
		8080, opt.GetEnv("ENDPOINT_HTTP_LISTENING_PORT"),
		opt.Description("The port in which the embedded HTTP server listens to"))
	opt.IntVar(&Flags.MqttTimeout, "mqttTimeout",
		60, opt.GetEnv("MQTT_TIMEOUT"),
		opt.Description("The number of seconds to wait before failing an outgoing MQTT message"))
	opt.BoolVar(&Flags.EndpointMqtt, "endpointMqtt",
		false, opt.GetEnv("ENDPOINT_MQTT"),
		opt.Description("Whether the embedded MQTT server is enabled"))
	opt.StringVar(&Flags.EndpointMqttListeningIP, "endpointMqttListeningIP",
		"127.0.0.1", opt.GetEnv("ENDPOINT_MQTT_LISTENING_IP"),
		opt.Description("The IP address where the embedded MQTT server listens to"))
	opt.IntVar(&Flags.EndpointMqttListeningPort, "endpointMqttListeningPort}",
		1883, opt.GetEnv("ENDPOINT_MQTT_LISTENING_PORT"),
		opt.Description("The port in which the embedded MQTT server listens to"))
	opt.BoolVar(&Flags.AutoUpdate, "autoUpdate", false,
		opt.GetEnv("AUTO_UPDATE"),
		opt.Description("A flag indicating whether the device should try to automatically obtain newer firmware"+
			" once per day"))
	opt.BoolVar(&Flags.SecureProvisioning, "secureProvisioning", false,
		opt.GetEnv("SECURE_PROVISIONING"),
		opt.Description("A flag indicating whether provisioning requests should be accompanied by a signature"+
			" token"))
	opt.StringVar(&Flags.SignatureAlgorithm, "signatureAlgorithm", "SHA256WITHRSA",
		opt.GetEnv("SIGNATURE_ALGORITHM"),
		opt.Description("The algorithm to use to produce signatures"))
	opt.StringVar(&Flags.VersionFile, "versionFile",
		filepath.Join(getHomeDir(), ".esthesis", "device", "version"),
		opt.GetEnv("VERSION_FILE"),
		opt.Description("A file with a single line of text depicting the current version of the firmware"+
			" running on the device"))
	opt.BoolVar(&Flags.VersionReport, "versionReport", false,
		opt.GetEnv("VERSION_REPORT"),
		opt.Description("Report the version number available in the specified version file"+
			" once during boot"))
	opt.StringVar(&Flags.VersionReportTopic, "versionReportTopic", "esthesis/metadata",
		opt.GetEnv("VERSION_REPORT_TOPIC"),
		opt.Description("The MQTT topic to report the firmware version"))
	opt.StringVar(&Flags.ProvisioningScript, "provisioningScript",
		filepath.Join(getHomeDir(), ".esthesis", "device", "firmware.sh"),
		opt.GetEnv("PROVISIONING_SCRIPT"),
		opt.Description("The script used to install new provisioning packages"))
	opt.StringVar(&Flags.RebootScript, "rebootScript",
		filepath.Join(getHomeDir(), ".esthesis", "device", "reboot.sh"),
		opt.GetEnv("REBOOT_SCRIPT"),
		opt.Description("The script used to reboot the device"))
	opt.StringVar(&Flags.ShutdownScript, "shutdownScript",
		filepath.Join(getHomeDir(), ".esthesis", "device", "shutdown.sh"),
		opt.GetEnv("SHUTDOWN_SCRIPT"),
		opt.Description("The script used to shutdown the device"))
	opt.StringVar(&Flags.SupportedCommands, "supportedCommands", "efrsph",
		opt.GetEnv("SUPPORTED_COMMANDS"),
		opt.Description("The remote commands this device supports:\n"+
			"e: Execute arbitrary\n"+
			"f: Firmware update\n"+
			"r: Reboot\n"+
			"s: Shutdown\n"+
			"p: Ping\n"+
			"h: Health report"))
	opt.StringVar(&Flags.TopicDemo, "topicDemo", "esthesis/telemetry",
		opt.GetEnv("TOPIC_DEMO"),
		opt.Description("The MQTT topic to post demo data"))
	opt.StringVar(&Flags.DemoCategory, "demoCategory", "demo",
		opt.GetEnv("DEMO_CATEGORY"),
		opt.Description("The category of data posted as demo data"))
	opt.IntVar(&Flags.DemoInterval, "demoInterval", 0,
		opt.GetEnv("DEMO_INTERVAL"),
		opt.Description("The frequency in which demo data is generated, in seconds"))
	opt.StringVar(&Flags.RegistrationSecret, "registrationSecret", "",
		opt.GetEnv("REGISTRATION_SECRET"),
		opt.Description("If set, the registration request will include it as a header"))
	opt.StringVar(&Flags.Attributes, "attributes", "",
		opt.GetEnv("ATTRIBUTES"),
		opt.Description("A comma-separated list of key-value pairs to be sent as attributes"))
	opt.BoolVar(&Flags.IgnoreHttpsInsecure, "ignoreHttpsInsecure", false,
		opt.GetEnv("IGNORE_HTTPS_INSECURE"),
		opt.Description("A flag to ignore HTTPS certificate errors"))
	opt.BoolVar(&Flags.IgnoreMqttInsecure, "ignoreMqttInsecure", false,
		opt.GetEnv("IGNORE_MQTT_INSECURE"),
		opt.Description("A flag to ignore MQTT certificate errors"))

	// Parse CLI arguments.
	_, err := opt.Parse(osArgs)
	if opt.Called("help") {
		fmt.Fprintf(os.Stderr, opt.Help())
		os.Exit(exitCodes.ExitHelp)
	}
	if opt.Called("version") {
		os.Exit(exitCodes.ExitVersion)
	}
	if err != nil {
		fmt.Fprintf(os.Stderr, "ERROR: %s\n\n", err)
		fmt.Fprintf(os.Stderr, opt.Help(getoptions.HelpSynopsis))
		os.Exit(exitCodes.ExitCliParse)
	}
	// Currently, only SHA256WITHRSA is supported.
	if Flags.SignatureAlgorithm != "SHA256WITHRSA" {
		log.Error("Only SHA256WITHRSA is supported for signatureAlgorithm.")
		os.Exit(exitCodes.ExitUnsupportedSignatureAlgorithm)
	}
}

// InitRegistrationProperties reads the properties provided during registration.
func InitRegistrationProperties() {
	var err error
	registrationProperties, err = properties.LoadAll(
		[]string{Flags.PropertiesFile, Flags.SecurePropertiesFile}, properties.UTF8, false)
	if err != nil {
		log.WithError(err).Errorf("Could not load agent registration properties")
		os.Exit(exitCodes.ExitCodeCouldNotLoadRegistrationProperties)
	}
}

func GetRegistrationProperty(propertyName string) string {
	prop, _ := registrationProperties.Get(propertyName)
	return prop
}
