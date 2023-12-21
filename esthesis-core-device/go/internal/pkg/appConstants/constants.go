package appConstants

type CommandType string
type CommandExecutionType string
type CommandSuccessType string

const (
	CommandTypeExec     CommandType = "e"
	CommandTypeFirmware CommandType = "f"
	CommandTypeReboot   CommandType = "r"
	CommandTypeShutdown CommandType = "s"
	CommandTypePing     CommandType = "p"
	CommandTypeHealth   CommandType = "h"
)

const (
	CommandExecutionTypeSynchronous  CommandExecutionType = "s"
	CommandExecutionTypeAsynchronous CommandExecutionType = "a"
)

const (
	CommandSuccessTypeSuccess CommandSuccessType = "s"
	CommandSuccessTypeFailure CommandSuccessType = "f"
)

const DeviceType = "ESTHESIS"

const RegistrationSecretHeaderName = "X-ESTHESIS-REGISTRATION-SECRET"
