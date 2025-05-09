package esthesis.dataflows.rdbmswriter.service;

import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.ValueData;
import esthesis.common.data.DataUtils.ValueType;
import esthesis.common.exception.QExceptionWrapper;
import esthesis.dataflows.rdbmswriter.config.AppConfig;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Utilities for writing data to the RDBMS.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class RdbmsService {

	@Inject
	AppConfig config;

	@Inject
	AgroalDataSource dataSource;

	/**
	 * Get the column names when using the multi-table update strategy.
	 *
	 * @param esthesisMessage The message to get the columns for.
	 * @return The columns for the multi-table strategy.
	 */
	private String getColumnsForMultiTableStrategy(
		EsthesisDataMessage esthesisMessage) {
		return
			config.dbStorageStrategyMultiHardwareIdName() + ", " +
				config.dbStorageStrategyMultiTimestampName() + ", " +
				esthesisMessage.getPayload().getValues().stream()
					.map(ValueData::getName).collect(Collectors.joining(", "));
	}

	/**
	 * Get the values when using the multi-table update strategy.
	 *
	 * @param esthesisMessage The message to get the values for.
	 * @return The values for the multi-table strategy.
	 */
	private String getValuesForMultiTableStrategy(
		EsthesisDataMessage esthesisMessage) {
		StringBuilder vals = new StringBuilder("?, ?, ");
		for (int i = 0; i < esthesisMessage.getPayload().getValues().size(); i++) {
			vals.append("?");
			if (i < esthesisMessage.getPayload().getValues().size() - 1) {
				vals.append(", ");
			}
		}

		return vals.toString();
	}

	/**
	 * Insert data into the database using the multi-table strategy.
	 *
	 * @param esthesisMessage The message to insert.
	 * @throws SQLException If an error occurs while inserting the data.
	 */
	@SuppressWarnings({"java:S2695", "java:S2077"})
	private void multi(EsthesisDataMessage esthesisMessage)
	throws SQLException {
		String statement =
			"INSERT INTO " + esthesisMessage.getPayload().getCategory() +
				" (" + getColumnsForMultiTableStrategy(esthesisMessage) + ")" +
				" VALUES (" + getValuesForMultiTableStrategy(esthesisMessage) + ")";
		try (Connection connection = dataSource.getConnection()) {
			try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
				preparedStatement.setString(1, esthesisMessage.getHardwareId());
				preparedStatement.setTimestamp(2,
					new Timestamp(
						Instant.parse(esthesisMessage.getPayload().getTimestamp())
							.toEpochMilli()));
				for (int i = 0; i < esthesisMessage.getPayload().getValues().size();
					i++) {
					ValueData valueData = esthesisMessage.getPayload().getValues().get(i);

					switch (ValueType.valueOf(valueData.getValueType().name())) {
						case STRING -> preparedStatement.setString(i + 3, valueData.getValue());
						case BOOLEAN ->
							preparedStatement.setBoolean(i + 3, BooleanUtils.toBoolean(valueData.getValue()));
						case BYTE -> preparedStatement.setByte(i + 3, NumberUtils.toByte(valueData.getValue()));
						case SHORT ->
							preparedStatement.setShort(i + 3, NumberUtils.toShort(valueData.getValue()));
						case INTEGER ->
							preparedStatement.setInt(i + 3, NumberUtils.toInt(valueData.getValue()));
						case LONG -> preparedStatement.setLong(i + 3, NumberUtils.toLong(valueData.getValue()));
						case FLOAT ->
							preparedStatement.setFloat(i + 3, NumberUtils.toFloat(valueData.getValue()));
						case DOUBLE ->
							preparedStatement.setDouble(i + 3, NumberUtils.toDouble(valueData.getValue()));
						case BIG_INTEGER, BIG_DECIMAL ->
							preparedStatement.setBigDecimal(i + 3, new BigDecimal(valueData.getValue()));
						case UNKNOWN -> {
							log.warn("Unknown value type '{}', settings value as String.",
								valueData.getValueType());
							preparedStatement.setString(i + 3, valueData.getValue());
						}
						default -> {
							log.warn("'{}' value type is not supported , settings value as String.",
								valueData.getValueType());
							preparedStatement.setString(i + 3, valueData.getValue());
						}
					}
				}
				log.debug("Executing statement '{}'.", preparedStatement);
				preparedStatement.execute();
			}
		}
	}

	/**
	 * Insert data into the database using the single-table strategy.
	 *
	 * @param esthesisMessage The message to insert.
	 * @throws SQLException If an error occurs while inserting the data.
	 */
	@SuppressWarnings("java:S2077")
	private void single(EsthesisDataMessage esthesisMessage)
	throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO " + config.dbStorageStrategySingleTableName()
					+ " (" + config.dbStorageStrategySingleHardwareIdName()
					+ ", " + config.dbStorageStrategySingleKeyName()
					+ ", " + config.dbStorageStrategySingleValueName()
					+ ", " + config.dbStorageStrategySingleTimestampName() + ") "
					+ "VALUES (?, ?, ?, ?)")) {
				statement.setString(1, esthesisMessage.getHardwareId());
				esthesisMessage.getPayload().getValues().forEach(data -> {
					try {
						statement.setString(2, data.getName());
						statement.setString(3, data.getValue());
						statement.setTimestamp(4, Timestamp.from(
							Instant.parse(esthesisMessage.getPayload().getTimestamp())));
						log.debug(statement.toString());
						statement.executeUpdate();
					} catch (SQLException e) {
						log.error("Error while inserting data into database.", e);
					}
				});
			} catch (SQLException e) {
				throw new QExceptionWrapper(e.getMessage(), e);
			}
		}
	}

	/**
	 * Processes a message from the Camel exchange.
	 *
	 * @param exchange The exchange to process the message from.
	 * @throws SQLException If an error occurs while processing the exchange.
	 */
	public void process(Exchange exchange) throws SQLException {
		// Get the message from the exchange.
		EsthesisDataMessage esthesisMessage = exchange.getIn().getBody(EsthesisDataMessage.class);
		log.debug("Processing '{}' with '{}' storage strategy.", esthesisMessage,
			config.dbStorageStrategy());

		// Process message.
		switch (config.dbStorageStrategy()) {
			case SINGLE -> single(esthesisMessage);
			case MULTI -> multi(esthesisMessage);
			default -> log.error("Unknown storage strategy '{}'.",
				config.dbStorageStrategy());
		}
	}
}
