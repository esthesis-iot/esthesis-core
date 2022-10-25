package esthesis.dataflows.rdbmswriter.service;

import esthesis.dataflow.common.DflUtils.VALUE_TYPE;
import esthesis.dataflow.common.parser.EsthesisMessage;
import esthesis.dataflow.common.parser.ValueData;
import esthesis.dataflows.rdbmswriter.config.AppConfig;
import io.agroal.api.AgroalDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

@Slf4j
@ApplicationScoped
public class RdbmsService {

  @Inject
  private AppConfig config;

  @Inject
  AgroalDataSource dataSource;

  private String getColumnsForMultiTableStrategy(
      EsthesisMessage esthesisMessage) {
    return
        config.dbStorageStrategyMultiHardwareIdName() + ", " +
            config.dbStorageStrategyMultiTimestampName() + ", " +
            esthesisMessage.getPayload().getValues().stream()
                .map(ValueData::getName).collect(Collectors.joining(", "));
  }

  private String getValuesForMultiTableStrategy(
      EsthesisMessage esthesisMessage) {
    StringBuilder vals = new StringBuilder("?, ?, ");
    for (int i = 0; i < esthesisMessage.getPayload().getValues().size(); i++) {
      vals.append("?");
      if (i < esthesisMessage.getPayload().getValues().size() - 1) {
        vals.append(", ");
      }
    }

    return vals.toString();
  }

  private void multi(EsthesisMessage esthesisMessage)
  throws SQLException {
    String statement =
        "INSERT INTO " + esthesisMessage.getPayload().getCategory() +
            " (" + getColumnsForMultiTableStrategy(esthesisMessage) + ")" +
            " VALUES (" + getValuesForMultiTableStrategy(esthesisMessage) + ")";
    try (Connection connection = dataSource.getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          statement)) {
        preparedStatement.setString(1, esthesisMessage.getHardwareId());
        preparedStatement.setTimestamp(2,
            new Timestamp(
                Instant.parse(esthesisMessage.getPayload().getTimestamp())
                    .toEpochMilli()));
        for (int i = 0; i < esthesisMessage.getPayload().getValues().size();
            i++) {
          ValueData valueData = esthesisMessage.getPayload().getValues().get(i);
          switch (VALUE_TYPE.valueOf(valueData.getValueType())) {
            case STRING -> preparedStatement.setString(i + 3,
                valueData.getValue());
            case BOOLEAN -> preparedStatement.setBoolean(i + 3,
                BooleanUtils.toBoolean(valueData.getValue()));
            case BYTE -> preparedStatement.setByte(i + 3,
                NumberUtils.toByte(valueData.getValue()));
            case SHORT -> preparedStatement.setShort(i + 3,
                NumberUtils.toShort(valueData.getValue()));
            case INTEGER -> preparedStatement.setInt(i + 3,
                NumberUtils.toInt(valueData.getValue()));
            case LONG -> preparedStatement.setLong(i + 3,
                NumberUtils.toLong(valueData.getValue()));
            case FLOAT -> preparedStatement.setFloat(i + 3,
                NumberUtils.toFloat(valueData.getValue()));
            case DOUBLE -> preparedStatement.setDouble(i + 3,
                NumberUtils.toDouble(valueData.getValue()));
            default -> log.warn("Unknown value type '{}', ignoring value '{}'.",
                valueData.getValueType(), valueData.getValue());
          }
        }
        log.debug("Executing statement '{}'.", preparedStatement);
        preparedStatement.execute();
      }
    }
  }

  private void single(EsthesisMessage esthesisMessage)
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
        throw new RuntimeException(e);
      }
    }
  }

  public void process(Exchange exchange) throws SQLException {
    // Get the message from the exchange.
    EsthesisMessage esthesisMessage = exchange.getIn()
        .getBody(EsthesisMessage.class);
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
