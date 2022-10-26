import {DATAFLOW_DEFINITION_PING_UPDATER} from "./dataflow-definitions/ping-updater";
import {DATAFLOW_DEFINITION_MQTT_CLIENT} from "./dataflow-definitions/mqtt-client";
import {DATAFLOW_DEFINITION_INFLUXDB_WRITER} from "./dataflow-definitions/influxdb-writer";
import {DATAFLOW_DEFINITION_RDBMS_WRITER} from "./dataflow-definitions/rdbms-writer";

export const dataflows = [
  DATAFLOW_DEFINITION_PING_UPDATER,
  DATAFLOW_DEFINITION_MQTT_CLIENT,
  DATAFLOW_DEFINITION_INFLUXDB_WRITER,
  DATAFLOW_DEFINITION_RDBMS_WRITER
];
