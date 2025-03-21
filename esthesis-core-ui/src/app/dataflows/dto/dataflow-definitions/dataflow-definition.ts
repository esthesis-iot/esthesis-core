import {DATAFLOW_DEFINITION_PING_UPDATER} from "./ping-updater";
import {DATAFLOW_DEFINITION_MQTT_CLIENT} from "./mqtt-client";
import {DATAFLOW_DEFINITION_INFLUXDB_WRITER} from "./influxdb-writer";
import {DATAFLOW_DEFINITION_RDBMS_WRITER} from "./rdbms-writer";
import {DATAFLOW_DEFINITION_REDIS_CACHE} from "./redis-cache";
import {DATAFLOW_DEFINITION_COMMAND_REPLY_UPDATER} from "./command-reply-updater";
import {DATAFLOW_DEFINITION_FIWARE_ORION} from "./orion-gateway";

export const dataflows = [
  DATAFLOW_DEFINITION_PING_UPDATER,
  DATAFLOW_DEFINITION_COMMAND_REPLY_UPDATER,
  DATAFLOW_DEFINITION_MQTT_CLIENT,
  DATAFLOW_DEFINITION_INFLUXDB_WRITER,
  DATAFLOW_DEFINITION_RDBMS_WRITER,
  DATAFLOW_DEFINITION_REDIS_CACHE,
  DATAFLOW_DEFINITION_FIWARE_ORION
];
