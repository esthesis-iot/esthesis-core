package esthesis.platform.server.nifi.client.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NifiConstants {

  public enum PATH {
    //root group
    ESTHESIS(new String[]{"[ESTHESIS]"}),

    //consumers
    CONSUMERS_PING_CONSUMER_HTTP(new String[]{"[ESTHESIS]", "[C]", "[PC]", "[H]"}),
    CONSUMERS_PING_CONSUMER_MQTT(new String[]{"[ESTHESIS]", "[C]", "[PC]", "[M]", "[C]"}),
    CONSUMERS_PING_WRITER(new String[]{"[ESTHESIS]", "[C]", "[PW]"}),

    CONSUMERS_METADATA_CONSUMER_MQTT(new String[]{"[ESTHESIS]", "[C]", "[MC]", "[M]", "[C]"}),
    CONSUMERS_METADATA_WRITER_INFLUXDB(new String[]{"[ESTHESIS]", "[C]", "[MW]", "[I]", "[W]"}),
    CONSUMERS_METADATA_WRITER_MYSQL(new String[]{"[ESTHESIS]", "[C]", "[MW]", "[M]", "[W]"}),

    CONSUMERS_TELEMETRY_CONSUMER_MQTT(new String[]{"[ESTHESIS]", "[C]", "[TC]", "[M]", "[C]"}),
    CONSUMERS_TELEMETRY_WRITER_INFLUXDB(new String[]{"[ESTHESIS]", "[C]", "[TW]", "[I]", "[W]"}),
    CONSUMERS_TELEMETRY_WRITER_MYSQL(new String[]{"[ESTHESIS]", "[C]", "[TW]", "[M]", "[W]"}),

    //producers
    PRODUCERS_INFLUXDB_READER_EXECUTOR(new String[]{"[ESTHESIS]", "[P]", "[R]", "[I]", "[E]"}),
    PRODUCERS_MYSQL_READER_EXECUTOR(new String[]{"[ESTHESIS]", "[P]", "[R]", "[M]", "[E]"});

    @Getter
    private final String[] path;

    public List<String> asList() {
      return Arrays.asList(path);
    }

    public String asString() {
      return asList().stream().collect(Collectors.joining(" > "));
    }

    PATH(String[] path) {
      this.path = path;
    }
  }

  @UtilityClass
  public static final class PORTS {

    //input ports
    public static final String CONSUMERS_METADATA_INFLUX_WRITERS_IN =
      "consumers_metadatawriters_influxdb_writers_in";
    public static final String CONSUMERS_METADATA_MYSQL_WRITERS_IN =
      "consumers_metadatawriters_mysql_writers_in";
    public static final String CONSUMERS_TELEMETRY_INFLUX_WRITERS_IN =
      "consumers_telemetrywriters_influxdb_writers_in";
    public static final String CONSUMERS_TELEMETRY_MYSQL_WRITERS_IN =
      "consumers_telemetrywriters_mysql_writers_in";

    //output ports
    public static final String CONSUMERS_PING_MQTT_CONSUMERS_OUT = "consumers_pingconsumers_mqtt_consumers_out";
    public static final String CONSUMERS_METADATA_MQTT_CONSUMERS_OUT =
      "consumers_metadataconsumers_mqtt_consumers_out";
    public static final String CONSUMERS_TELEMETRY_MQTT_CONSUMERS_OUT =
      "consumers_telemetryconsumers_mqtt_consumers_out";
    public static final String CONSUMERS_METADATA_INFLUX_WRITERS_LOGOUT =
      "consumers_metadatawriters_influxdb_writers_logout";
    public static final String CONSUMERS_METADATA_MYSQL_WRITERS_LOGOUT =
      "consumers_metadatawriters_mysql_writers_logout";
    public static final String CONSUMERS_TELEMETRY_INFLUX_WRITERS_LOGOUT =
      "consumers_telemetrywriters_influxdb_writers_logout";
    public static final String CONSUMERS_TELEMETRY_MYSQL_WRITERS_LOGOUT =
      "consumers_telemetrywriters_mysql_writers_logout";
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
    public static final class Type {

      public static final String MQTT_CONSUME = "org.apache.nifi.processors.mqtt.ConsumeMQTT";
      public static final String PUT_INFLUX_DB = "org.apache.nifi.processors.influxdb.PutInfluxDB";
      public static final String PUT_DATABASE_RECORD = "org.apache.nifi.processors.standard.PutDatabaseRecord";

    }
  }

  @UtilityClass
  public static final class Properties {

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
    public static final String PUT_DB_RECORD_READER = "put-db-record-record-reader";
    public static final String PUT_DB_RECORD_STATEMENT_TYPE = "put-db-record-statement-type";
    public static final String PUT_DB_RECORD_DCBP_SERVICE = "put-db-record-dcbp-service";
    public static final String PUT_DB_RECORD_TABLE_NAME = "put-db-record-table-name";
    public static final String AUTO_TERMINATED_RELATIONSHIPS = "autoTerminatedRelationships";


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
      public enum TIME_UNIT {
        days, hrs, millis, mins, nanos, secs
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
      public enum RELATIONSHIP_TYPE {
        MESSAGE("Message"),
        FAILURE("failure"),
        FAILURE_MAX_SIZE("failure-max-size"),
        RETRY("retry"),
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
