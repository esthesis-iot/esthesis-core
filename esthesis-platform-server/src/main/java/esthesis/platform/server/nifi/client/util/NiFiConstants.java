package esthesis.platform.server.nifi.client.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;

public class NiFiConstants {

  public enum PATH {
    //root group
    ESTHESIS(new String[]{"[ESTHESIS]"}),

    //sink types
    READERS(new String[]{"[R]"}),
    PRODUCERS(new String[]{"[P]"}),
    WRITERS(new String[]{"[W]"}),
    LOGGERS(new String[]{"[L]"}),
    INSTANCES(new String[]{"[I]"});

    @Getter
    private final String[] groupPath;

    public List<String> asList() {
      return Arrays.asList(groupPath);
    }

    public String asString() {
      return String.join(" > ", asList());
    }

    PATH(String[] groupPath) {
      this.groupPath = groupPath;
    }
  }

  @UtilityClass
  public static final class SyncErrors {

    public static final String NON_EXISTENT_PROCESSOR = "Does not exist in the NiFi workflow.";
  }

  @UtilityClass
  public static final class Bundle {

    @UtilityClass
    public static final class BundleGroup {

      public static final String NIFI = "org.apache.nifi";
      public static final String ESTHESIS = "esthesis.docker.nifi.extensions";
    }

    @UtilityClass
    public static final class BundleArtifact {

      public static final String SSL_CONTEXT = "nifi-ssl-context-service-nar";
      public static final String DBCP = "nifi-dbcp-service-nar";
      public static final String MQTT = "nifi-mqtt-nar";
      public static final String INFLUX_DB = "nifi-influxdb-nar";
      public static final String STANDARD = "nifi-standard-nar";
      public static final String RECORD_SERIALIZATION = "nifi-record-serialization-services-nar";

    }
  }

  @UtilityClass
  public static final class ControllerService {

    @UtilityClass
    public static final class Type {

      public static final String SSL_CONTEXT = "org.apache.nifi.ssl.StandardRestrictedSSLContextService";
      public static final String DBCP_POOL = "org.apache.nifi.dbcp.DBCPConnectionPool";
      public static final String JSON_TREE_READER = "org.apache.nifi.json.JsonTreeReader";
    }
  }

  @UtilityClass
  public static final class Processor {

    @UtilityClass
    public static final class UrlPaths {

      public static final String PROCESS_GROUPS = "process-groups";
      public static final String CONTROLLER_SERVICES = "controller-services";
      public static final String FLOW = "flow";
      public static final String PROCESSORS = "processors";
      public static final String RUN_STATUS = "/run-status/";
      public static final String VERSION = "?version=";
      public static final String CLIENT_ID = "&clientId=";
    }

    @UtilityClass
    public static final class Type {

      public static final String MQTT_PUBLISH = "org.apache.nifi.processors.mqtt.PublishMQTT";
      public static final String MQTT_CONSUME = "org.apache.nifi.processors.mqtt.ConsumeMQTT";
      public static final String PUT_INFLUX_DB = "org.apache.nifi.processors.influxdb.PutInfluxDB";
      public static final String PUT_DATABASE_RECORD = "org.apache.nifi.processors.standard.PutDatabaseRecord";
      public static final String EXECUTE_INFLUX_DB = "org.apache.nifi.processors.influxdb.ExecuteInfluxDBQuery";
      public static final String EXECUTE_SQL = "org.apache.nifi.processors.standard.ExecuteSQL";
      public static final String DISTRIBUTE_LOAD = "org.apache.nifi.processors.standard.DistributeLoad";
      public static final String PUT_FILE = "org.apache.nifi.processors.standard.PutFile";
      public static final String PUT_SYSLOG = "org.apache.nifi.processors.standard.PutSyslog";
      public static final String STANDARD_HTTP_CONTEXT_MAP = "org.apache.nifi.http"
        + ".StandardHttpContextMap";
      public static final String PUT_SQL = "org.apache.nifi.processors.standard.PutSQL";
    }
  }

  @UtilityClass
  public static final class Properties {

    public static final String TOPIC = "Topic";
    public static final String RETAIN_MSG = "Retain Message";
    public static final String BROKER_URI = "Broker URI";
    public static final String CLIENT_ID = "Client ID";
    public static final String DB_CONNECTION_URL = "Database Connection URL";
    public static final String DB_DRIVER_CLASS_NAME = "Database Driver Class Name";
    public static final String DB_DRIVER_LOCATION = "database-driver-locations";
    public static final String DB_USER = "Database User";
    public static final String KEYSTORE_FILENAME = "Keystore Filename";
    public static final String KEYSTORE_PSWD = "Keystore Password";
    public static final String KEYSTORE_TYPE = "Keystore Type";
    public static final String MAX_QUEUE_SIZE = "Max Queue Size";
    public static final String PSWD = "Password";
    public static final String QOS = "Quality of Service(QoS)";
    public static final String SSL_CONTEXT_SERVICE = "SSL Context Service";
    public static final String SSL_PROTOCOL = "SSL Protocol";
    public static final String TOPIC_FILTER = "Topic Filter";
    public static final String TRUSTSTORE_FILENAME = "Truststore Filename";
    public static final String TRUSTSTORE_PSWD = "Truststore Password";
    public static final String TRUSTSTORE_TYPE = "Truststore Type";
    public static final String INFLUX_DB_NAME = "influxdb-dbname";
    public static final String INFLUX_URL = "influxdb-url";
    public static final String INFLUX_MAX_CONNECTION_TIMEOUT = "InfluxDB Max Connection Time Out (seconds)";
    public static final String INFLUX_USERNAME = "influxdb-username";
    public static final String INFLUX_PASSWORD = "influxdb-password";
    public static final String INFLUX_CHARSET = "influxdb-charset";
    public static final String INFLUX_CONSISTENCY_LEVEL = "influxdb-consistency-level";
    public static final String INFLUX_RETENTION_POLICY = "influxdb-retention-policy";
    public static final String INFLUX_MAX_RECORDS_SIZE = "influxdb-max-records-size";

    public static final String INFLUX_QUERY_RESULT_TIME_UNIT = "influxdb-query-result-time-unit";
    public static final String INFLUX_QUERY_CHUNK_SIZE = "influxdb-query-chunk-size";

    public static final String PUT_DB_RECORD_READER = "put-db-record-record-reader";
    public static final String PUT_DB_RECORD_STATEMENT_TYPE = "put-db-record-statement-type";
    public static final String PUT_DB_RECORD_DCBP_SERVICE = "put-db-record-dcbp-service";
    public static final String PUT_DB_RECORD_TABLE_NAME = "put-db-record-table-name";
    public static final String PUT_DB_RECORD_TRANSLATE_FIELD_NAMES = "put-db-record-translate"
      + "-field-names";
    public static final String PUT_DB_RECORD_FIELD_CONTAINING_SQL = "put-db-record-field-containing-sql";

    public static final String DCBP_SERVICE = "Database Connection Pooling Service";

    public static final String DIRECTORY = "Directory";
    public static final String CONFLICT_RESOLUTION_STRATEGY = "Conflict Resolution Strategy";

    public static final String HOSTNAME = "Hostname";
    public static final String PORT = "Port";
    public static final String PROTOCOL = "Protocol";
    public static final String MESSAGE_BODY = "Message Body";
    public static final String MESSAGE_PRIORITY = "Message Priority";

    public static final String JDBC_CONNECTION_POOL = "JDBC Connection Pool";
    public static final String PUT_SQL_STATEMENT = "putsql-sql-statement";
    public static final String COMMAND_REQUEST_INSERT_QUERY = "insert into command_request"
      + "(created_by, created_on, version, operation, description, args, device_id) values('${esthesis.command.createdBy}', "
      + "'${now():toDate():format('yyyy-MM-dd HH:mm:ss')}', 0, '${esthesis.operation}', '${esthesis.command.description}', "
      + "'${esthesis.command.args}', ${esthesis.command.deviceId});";

    public static final String SQL_SELECT_QUERY = "SQL select query";
    public static final String DEVICE_ID_QUERY = "select id from device where hardware_id = '${esthesis.hardwareId}'";

    @UtilityClass
    public static final class Values {

      @AllArgsConstructor
      @Getter
      public enum KEY_TRUST_STORE_TYPE {
        JKS("JKS"),
        PKCS12("PKCS12");

        private final String type;
      }

      @AllArgsConstructor
      @Getter
      public enum DATA_UNIT {
        B, KB, MB, GB, TB
      }

      @AllArgsConstructor
      @Getter
      public enum CONSISTENCY_LEVEL {
        ONE, ANY, ALL, QUOROM
      }

      @AllArgsConstructor
      @Getter
      public enum STATEMENT_TYPE {
        UPDATE("UPDATE"),
        INSERT("INSERT"),
        DELETE("DELETE"),
        USE_STATE_ATTRIBUTE("Use statement.type Attribute");

        private final String type;

      }

      @AllArgsConstructor
      @Getter
      public enum CONNECTABLE_COMPONENT_TYPE {
        PROCESSOR, REMOTE_INPUT_PORT, REMOTE_OUTPUT_PORT, INPUT_PORT, OUTPUT_PORT, FUNNEL
      }

      @AllArgsConstructor
      @Getter
      public enum FAILED_RELATIONSHIP_TYPES {

        FAILURE("failure"),
        FAILURE_MAX_SIZE("failure-max-size"),
        RETRY("retry"),
        INVALID("invalid");

        private final String type;
      }

      @AllArgsConstructor
      @Getter
      public enum SUCCESSFUL_RELATIONSHIP_TYPES {
        MESSAGE("Message"),
        SUCCESS("success");

        private final String type;
      }

      @AllArgsConstructor
      @Getter
      public enum STATE {
        RUNNING, STOPPED, ENABLED, DISABLED
      }
    }
  }
}
