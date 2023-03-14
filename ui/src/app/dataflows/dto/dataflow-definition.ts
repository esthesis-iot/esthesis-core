import {DATAFLOW_DEFINITION_PING_UPDATER} from "./dataflow-definitions/ping-updater";
import {DATAFLOW_DEFINITION_MQTT_CLIENT} from "./dataflow-definitions/mqtt-client";
import {DATAFLOW_DEFINITION_INFLUXDB_WRITER} from "./dataflow-definitions/influxdb-writer";
import {DATAFLOW_DEFINITION_RDBMS_WRITER} from "./dataflow-definitions/rdbms-writer";
import {DATAFLOW_DEFINITION_REDIS_CACHE} from "./dataflow-definitions/redis-cache";
import {
  DATAFLOW_DEFINITION_COMMAND_REPLY_UPDATER
} from "./dataflow-definitions/command-reply-updater";
import {DATAFLOW_DEFINITION_FIWARE_ORION} from "./dataflow-definitions/orion-gateway";

export const dataflows = [
  DATAFLOW_DEFINITION_PING_UPDATER,
  DATAFLOW_DEFINITION_COMMAND_REPLY_UPDATER,
  DATAFLOW_DEFINITION_MQTT_CLIENT,
  DATAFLOW_DEFINITION_INFLUXDB_WRITER,
  DATAFLOW_DEFINITION_RDBMS_WRITER,
  DATAFLOW_DEFINITION_REDIS_CACHE,
  DATAFLOW_DEFINITION_FIWARE_ORION
];
