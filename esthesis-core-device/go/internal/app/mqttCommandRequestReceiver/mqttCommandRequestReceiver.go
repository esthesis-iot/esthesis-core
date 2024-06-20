package mqttCommandRequestReceiver

import (
	"errors"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/esthesis-iot/esthesis-device/internal/app/autoUpdate"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/appConstants"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/channels"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/config"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/dto"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/util"
	log "github.com/sirupsen/logrus"
	"io"
	"os/exec"
	"strings"
)

func serialiseCommandReply(commandReply *dto.CommandReply) string {
	return strings.Join([]string{
		commandReply.CorrelationId,
		string(func() appConstants.CommandSuccessType {
			if commandReply.Success {
				return appConstants.CommandSuccessTypeSuccess
			} else {
				return appConstants.CommandSuccessTypeFailure
			}
		}()), commandReply.Output}, " ")
}

func executeCommand(command *dto.CommandRequest, client mqtt.Client) {
	// Prepare the command to be executed.
	cmd := exec.Command(command.Command)
	for _, arg := range command.Arguments {
		cmd.Args = append(cmd.Args, arg)
	}
	runCommand(cmd, command, client)
}

// Function to publish a command reply
func publishCommandReply(commandReply dto.CommandReply, client mqtt.Client) {
	var commandReplyTopic = config.Flags.TopicCommandReply + "/" + config.Flags.HardwareId
	var replyText = serialiseCommandReply(&commandReply)
	log.Debugf("Publishing to topic '%s' command reply '%s' .",
		commandReplyTopic, util.AbbrS(replyText))
	token := client.Publish(commandReplyTopic, 0, false, replyText)
	token.Wait()
}

// Run a command sync or async depending on its defined ExecutionType
func runCommand(cmd *exec.Cmd, command *dto.CommandRequest, client mqtt.Client) {
	// According to the execution type of the command, we either fire-and-forget
	// or wait to collect the results.
	if command.ExecutionType == appConstants.CommandExecutionTypeAsynchronous {
		runCommandAsync(cmd, command, client)
	} else if command.ExecutionType == appConstants.CommandExecutionTypeSynchronous {
		runCommandSync(cmd, command, client)
	} else {
		log.Errorf("Unknown command type '%s'.", command.ExecutionType)
	}
}

// Function to run a command asynchronously
func runCommandAsync(cmd *exec.Cmd, command *dto.CommandRequest, client mqtt.Client) {
	// Create pipes for stdout and stderr
	stdoutPipe, err := cmd.StdoutPipe()
	if err != nil {
		log.WithError(err).Errorf("Could not create stdout pipe for asynchronous command.")
		commandReply := dto.CommandReply{
			CorrelationId: command.Id,
			Success:       false,
			Output:        err.Error(),
		}
		publishCommandReply(commandReply, client)
		return
	}
	stderrPipe, err := cmd.StderrPipe()
	if err != nil {
		log.WithError(err).Errorf("Could not create stderr pipe for asynchronous command.")
		commandReply := dto.CommandReply{
			CorrelationId: command.Id,
			Success:       false,
			Output:        err.Error(),
		}
		publishCommandReply(commandReply, client)
		return
	}

	// Start the command
	err = cmd.Start()
	if err != nil {
		log.WithError(err).Errorf("Could not start asynchronous command.")
		commandReply := dto.CommandReply{
			CorrelationId: command.Id,
			Success:       false,
			Output:        err.Error(),
		}
		publishCommandReply(commandReply, client)
		return
	}

	// Read the output in a separate goroutine
	go func() {
		// Capture stdout
		stdout, _ := io.ReadAll(stdoutPipe)
		// Capture stderr
		stderr, _ := io.ReadAll(stderrPipe)

		err = cmd.Wait()
		var commandReply dto.CommandReply
		commandReply.CorrelationId = command.Id
		if err != nil {
			log.WithError(err).Errorf("Could not complete asynchronous command.")
			commandReply.Success = false
			commandReply.Output = err.Error() + ": " + string(stderr)
		} else {
			commandReply.Success = true
			commandReply.Output = strings.TrimSpace(string(stdout))
		}
		publishCommandReply(commandReply, client)
	}()
}

// Function to run a command synchronously
func runCommandSync(cmd *exec.Cmd, command *dto.CommandRequest, client mqtt.Client) {
	out, err := cmd.Output()
	// Send a reply with the results of this command.
	var commandReply dto.CommandReply
	commandReply.CorrelationId = command.Id
	if err != nil {
		log.WithError(err).Errorf("Could not execute synchronous command.")
		commandReply.Success = false
		commandReply.Output = err.Error()
	} else {
		commandReply.Success = true
		commandReply.Output = strings.TrimSpace(string(out))
	}
	publishCommandReply(commandReply, client)
}

func printCommandParsingError(payload []byte) {
	log.Errorf("Could not process incoming command '%s', "+
		"wrong format. Command format is:"+
		"[id] [commandType][executionType] {command} {arguments}.",
		util.AbbrBA(payload))
}

func debugPrintCommand(command *dto.CommandRequest) {
	log.Debugf("Parsed command:\n"+
		"  Id: %s\n"+
		"  Type: %s\n"+
		"  ExecutionType: %s\n"+
		"  Command: %s\n"+
		"  Arguments: %s\n",
		command.Id, command.CommandType, command.ExecutionType,
		util.AbbrS(command.Command), util.AbbrSA(command.Arguments))
}

func parseCommandRequest(body []byte) (dto.CommandRequest, error) {
	var commandRequest dto.CommandRequest
	var bodySplit = strings.Split(string(body), " ")

	// Check if the command can be properly parsed.
	if len(bodySplit) < 2 {
		printCommandParsingError(body)
		return commandRequest, errors.New("could not parse command")
	}

	// Find the ID, command type, and execution type. These are mandatory parts of a command.
	commandRequest.Id = bodySplit[0]
	commandRequest.CommandType = appConstants.CommandType(bodySplit[1][0:1])
	commandRequest.ExecutionType = appConstants.CommandExecutionType(bodySplit[1][1:2])

	// Find the command and arguments. These are optional parts of a command.
	if len(bodySplit) > 2 {
		commandRequest.Command = bodySplit[2]
	}
	if len(bodySplit) > 3 {
		commandRequest.Arguments = bodySplit[3:]
	}

	return commandRequest, nil
}

func rebootCommand(command *dto.CommandRequest, client mqtt.Client) {
	rebootScript := config.Flags.RebootScript
	if rebootScript == "" {
		log.Warn("Received a reboot command but no reboot script is configured. " +
			"Command will be ignored.")
		publishCommandReply(dto.CommandReply{
			CorrelationId: command.Id,
			Success:       false,
			Output:        "no reboot script is configured"}, client)
	} else {
		if util.IsFileExists(rebootScript) {
			log.Debugf("Executing reboot command using script '%s'.", rebootScript)
			cmd := exec.Command(rebootScript)
			runCommand(cmd, command, client)
		} else {
			log.Warnf("Received a reboot command but the reboot script '%s' does not exist. "+
				"Command will be ignored.", rebootScript)
			publishCommandReply(dto.CommandReply{
				CorrelationId: command.Id,
				Success:       false,
				Output:        "reboot script does not exist."}, client)
		}
	}
}

func shutdownCommand(command *dto.CommandRequest, client mqtt.Client) {
	shutdownScript := config.Flags.ShutdownScript
	if shutdownScript == "" {
		log.Warn("Received a shutdown command but no shutdown script is configured. " +
			"Command will be ignored.")
		publishCommandReply(dto.CommandReply{
			CorrelationId: command.Id,
			Success:       false,
			Output:        "no shutdown script is configured"}, client)
	} else {
		if util.IsFileExists(shutdownScript) {
			log.Debugf("Executing shutdown command using script '%s'.", shutdownScript)
			cmd := exec.Command(shutdownScript)
			runCommand(cmd, command, client)
		} else {
			log.Warnf("Received a shutdown command but the shutdown script '%s' does not exist. "+
				"Command will be ignored.", shutdownScript)
			publishCommandReply(dto.CommandReply{
				CorrelationId: command.Id,
				Success:       false,
				Output:        "shutdown script does not exist."}, client)
		}
	}
}

func updateCommand(command *dto.CommandRequest, client mqtt.Client) {
	if autoUpdate.IsUpdateInProgress() {
		warnMessage := "Update already in progress, ignoring command."
		log.Warn(warnMessage)
		publishCommandReply(dto.CommandReply{
			CorrelationId: command.Id,
			Success:       false,
			Output:        warnMessage}, client)
	} else {
		msg, err := autoUpdate.Update(command.Command)

		if err != nil {
			publishCommandReply(dto.CommandReply{
				CorrelationId: command.Id,
				Success:       false,
				Output:        err.Error()}, client)
		} else {
			publishCommandReply(dto.CommandReply{
				CorrelationId: command.Id,
				Success:       true,
				Output:        msg}, client)
		}
	}
}

func pingCommand(command *dto.CommandRequest, client mqtt.Client) {
	log.Warn("Executing ping command... ")
	channels.GetPingChan() <- false
	publishCommandReply(dto.CommandReply{
		CorrelationId: command.Id,
		Success:       true,
		Output:        "PING command was sent to the device."}, client)

}

func healthCommand(command *dto.CommandRequest, client mqtt.Client) {
	log.Warn("Executing health command... ")
	channels.GetHealthChan() <- false
	publishCommandReply(dto.CommandReply{
		CorrelationId: command.Id,
		Success:       true,
		Output:        "HEALTH command was sent to the device."}, client)

}

func OnMessage(client mqtt.Client, msg mqtt.Message) {
	log.Debugf("Received command request message '%s'.", util.AbbrBA(msg.Payload()))

	// Attempt to parse the message.
	commandRequest, err := parseCommandRequest(msg.Payload())
	if err != nil {
		log.WithError(err).Errorf("Could not parse command request.")
		return
	}
	debugPrintCommand(&commandRequest)

	// Execute the command.
	if strings.Contains(config.Flags.SupportedCommands, string(commandRequest.CommandType)) {
		switch commandRequest.CommandType {
		case appConstants.CommandTypeExec:
			go executeCommand(&commandRequest, client)
		case appConstants.CommandTypePing:
			pingCommand(&commandRequest, client)
		case appConstants.CommandTypeHealth:
			healthCommand(&commandRequest, client)
		case appConstants.CommandTypeReboot:
			rebootCommand(&commandRequest, client)
		case appConstants.CommandTypeShutdown:
			shutdownCommand(&commandRequest, client)
		case appConstants.CommandTypeFirmware:
			updateCommand(&commandRequest, client)
		}
	} else {
		log.Errorf("Unsupported command type '%s'.", commandRequest.CommandType)
	}
}
