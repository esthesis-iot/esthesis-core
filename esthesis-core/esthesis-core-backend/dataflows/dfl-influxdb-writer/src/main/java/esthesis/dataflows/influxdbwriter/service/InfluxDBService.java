package esthesis.dataflows.influxdbwriter.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import esthesis.avro.EsthesisDataMessage;
import esthesis.common.data.ValueUtils.ValueType;
import esthesis.dataflows.influxdbwriter.config.AppConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

@Slf4j
@Transactional
@ApplicationScoped
public class InfluxDBService {

	// The name of the InfluxDB tag to use to for the hardware id of each device.
	private static final String INFLUXDB_TAG_HARDWARE_ID = "hardwareId";

	@Inject
	AppConfig config;
	InfluxDBClient influxDBClient;

	@PostConstruct
	void init() {
		reconnectClient();
	}

	@PreDestroy
	void destroy() {
		if (influxDBClient != null) {
			log.debug("Shutting down client, closing existing InfluxDB connection.");
			influxDBClient.close();
		}
	}

	private void reconnectClient() {
		if (influxDBClient != null) {
			log.debug("Reconnect client, closing existing InfluxDB connection.");
			influxDBClient.close();
		}

		log.debug("Connecting to InfluxDB at '{}', org '{}', and bucket '{}'.",
			config.influxUrl(), config.influxOrg(), config.influxBucket());
		influxDBClient = InfluxDBClientFactory
			.create(config.influxUrl(), config.influxToken().toCharArray(),
				config.influxOrg(), config.influxBucket());
	}

	public void process(Exchange exchange) {
		// Get the message from the exchange.
		EsthesisDataMessage esthesisMessage = exchange.getIn()
			.getBody(EsthesisDataMessage.class);

		// Create an InfluxDB point for the message.
		Point point = new Point(esthesisMessage.getPayload().getCategory());
		point.addTag(INFLUXDB_TAG_HARDWARE_ID, esthesisMessage.getHardwareId());
		if (esthesisMessage.getPayload().getTimestamp() != null) {
			point.time(Instant.parse(esthesisMessage.getPayload().getTimestamp()),
				WritePrecision.NS);
		} else {
			if (esthesisMessage.getSeenAt() != null) {
				point.time(Instant.parse(esthesisMessage.getSeenAt()),
					WritePrecision.MS);
			} else {
				point.time(System.currentTimeMillis(), WritePrecision.MS);
			}
		}

		// Set the values of the point.
		esthesisMessage.getPayload().getValues().forEach(keyValue -> {
			String name = keyValue.getName();
			String value = keyValue.getValue();
			ValueType valueType = ValueType.valueOf(keyValue.getValueType().name());
			switch (valueType) {
				case STRING, BIG_INTEGER, BIG_DECIMAL -> point.addField(name, value);
				case BOOLEAN -> point.addField(name, BooleanUtils.toBoolean(value));
				case BYTE -> point.addField(name, NumberUtils.toByte(value));
				case SHORT -> point.addField(name, NumberUtils.toShort(value));
				case INTEGER -> point.addField(name, NumberUtils.toInt(value));
				case LONG -> point.addField(name, NumberUtils.toLong(value));
				case FLOAT -> point.addField(name, NumberUtils.toFloat(value));
				case DOUBLE -> point.addField(name, NumberUtils.toDouble(value));
				default -> {
					log.warn("Unknown value type '{}', reverting to String type.", valueType);
					point.addField(name, value);
				}
			}
		});

		// Write the point to InfluxDB.
		log.debug("Writing point '{}'.", point.toLineProtocol());
		WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
		writeApi.writePoint(point);
	}
}
