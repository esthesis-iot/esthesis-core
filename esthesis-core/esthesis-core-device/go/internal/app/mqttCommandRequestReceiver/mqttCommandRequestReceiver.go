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
	var commandReplyTopic = config.Flags.TopicCommandReply + "/" + config.Flags.HardwareId
	// Prepare the command to be executed.
	cmd := exec.Command(command.Command)
	for _, arg := range command.Arguments {
		cmd.Args = append(cmd.Args, arg)
	}

	// According to the execution type of the command, we either fire-and-forget
	// or wait to collect the results.
	if command.ExecutionType == appConstants.CommandExecutionTypeAsynchronous {
		err := cmd.Start()
		if err != nil {
			log.Errorf("Could not execute asynchronous command due to '%s'.", err)
		}
	} else if command.ExecutionType == appConstants.CommandExecutionTypeSynchronous {
		out, err := cmd.Output()
		// Send a reply with the results of this command.
		var commandReply dto.CommandReply
		commandReply.CorrelationId = command.Id
		if err != nil {
			log.Errorf("Could not execute synchronous command due to '%s'.",
				err)
			commandReply.Success = false
			commandReply.Output = err.Error()
		} else {
			commandReply.Success = true
			commandReply.Output = strings.TrimSpace(string(out))
		}

		var replyText = serialiseCommandReply(&commandReply)
		log.Debugf("Publishing to topic '%s' command reply '%s' .",
			commandReplyTopic, util.AbbrS(replyText))
		token := client.Publish(commandReplyTopic, 0, false, replyText)
		token.Wait()
	} else {
		log.Errorf("Unknown command type '%s'.", command.ExecutionType)
	}
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

func rebootCommand() {
	rebootScript := config.Flags.RebootScript
	if rebootScript == "" {
		log.Warn("Received a reboot command but no reboot script is configured. " +
			"Command will be ignored.")
	} else {
		if util.IsFileExists(rebootScript) {
			log.Debugf("Executing reboot command using script '%s'.", rebootScript)
			cmd := exec.Command(rebootScript)
			cmd.Start()
		} else {
			log.Warnf("Received a reboot command but the reboot script '%s' does not exist. "+
				"Command will be ignored.", rebootScript)
		}
	}
}

func shutdownCommand() {
	shutdownScript := config.Flags.ShutdownScript
	if shutdownScript == "" {
		log.Warn("Received a shutdown command but no shutdown script is configured. " +
			"Command will be ignored.")
	} else {
		if util.IsFileExists(shutdownScript) {
			log.Debugf("Executing shutdown command using script '%s'.", shutdownScript)
			cmd := exec.Command(shutdownScript)
			cmd.Start()
		} else {
			log.Warnf("Received a shutdown command but the shutdown script '%s' does not exist. "+
				"Command will be ignored.", shutdownScript)
		}
	}
}

func OnMessage(client mqtt.Client, msg mqtt.Message) {
	log.Debugf("Received command request message '%s'.",
		util.AbbrBA(msg.Payload()))

	// Attempt to parse the message.
	commandRequest, err := parseCommandRequest(msg.Payload())
	if err != nil {
		log.Errorf("Could not parse command request due to '%s'.", err)
		return
	}
	debugPrintCommand(&commandRequest)

	// Execute the command.
	if strings.Contains(config.Flags.SupportedCommands, string(commandRequest.CommandType)) {
		switch commandRequest.CommandType {
		case appConstants.CommandTypeExec:
			go executeCommand(&commandRequest, client)
		case appConstants.CommandTypePing:
			channels.GetPingChan() <- false
		case appConstants.CommandTypeHealth:
			channels.GetHealthChan() <- false
		case appConstants.CommandTypeReboot:
			rebootCommand()
		case appConstants.CommandTypeShutdown:
			shutdownCommand()
		case appConstants.CommandTypeFirmware:
			if autoUpdate.IsUpdateInProgress() {
				log.Warn("Update already in progress, ignoring command.")
				return
			} else {
				autoUpdate.Update(commandRequest.Command)
			}
		}
	} else {
		log.Errorf("Unsupported command type '%s'.", commandRequest.CommandType)
	}
}
