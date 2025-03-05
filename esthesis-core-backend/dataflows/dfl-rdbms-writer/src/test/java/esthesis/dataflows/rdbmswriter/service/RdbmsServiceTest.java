package esthesis.dataflows.rdbmswriter.service;

import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import esthesis.dataflows.rdbmswriter.config.AppConfig;
import esthesis.dataflows.rdbmswriter.config.AppConfig.STORAGE_STRATEGY;
import io.agroal.api.AgroalDataSource;
import lombok.SneakyThrows;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RdbmsServiceTest {

	@Mock
	private AppConfig config;

	@Mock
	private Exchange exchange;

	@Mock
	private Message message;

	@Mock
	private AgroalDataSource dataSource;

	@Mock
	Connection connection;

	@Mock
	PreparedStatement preparedStatement;

	@InjectMocks
	RdbmsService rdbmsService;

	@SneakyThrows
	@BeforeEach
	void setUp() {
		// Initialize mocks.
		MockitoAnnotations.openMocks(this);

		// Set up common mock behaviors.
		when(exchange.getIn()).thenReturn(message);
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage());
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

		// Mock PreparedStatement execution methods.
		doNothing().when(preparedStatement).setString(anyInt(), anyString());
		doNothing().when(preparedStatement).setTimestamp(anyInt(), any(Timestamp.class));


	}

	@SneakyThrows
	@Test
	void process_WithSingleStrategy() {
		// Mock PreparedStatement execution methods.
		when(preparedStatement.executeUpdate()).thenReturn(1);

		// Set up the configuration for SINGLE storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.SINGLE);
		when(config.dbStorageStrategySingleTableName()).thenReturn("testTableName");
		when(config.dbStorageStrategySingleHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategySingleKeyName()).thenReturn("testKeyName");
		when(config.dbStorageStrategySingleValueName()).thenReturn("testValueName");
		when(config.dbStorageStrategySingleTimestampName()).thenReturn("testTimestampName");

		// Call the process method with the exchange.
		rdbmsService.process(exchange);

		// Verify that the correct SQL statement was prepared and executed.
		verify(connection).prepareStatement(
			"INSERT INTO testTableName (testHardwareIdName, testKeyName, testValueName, testTimestampName) " +
				"VALUES (?, ?, ?, ?)");
		verify(preparedStatement).setString(1, "testHardwareId");
		verify(preparedStatement).setString(2, "test-measurement");
		verify(preparedStatement).setString(3, "10");
		verify(preparedStatement).setTimestamp(4, Timestamp.from(Instant.parse("2025-02-17T12:00:00.123Z")));
		verify(preparedStatement).executeUpdate();
	}

	@SneakyThrows
	@Test
	void process_WithMultiStrategy() {
		// Set up the configuration for MULTI storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.MULTI);
		when(config.dbStorageStrategyMultiHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategyMultiTimestampName()).thenReturn("testTimestampName");

		// Mock PreparedStatement methods to do nothing.
		doNothing().when(preparedStatement).setInt(anyInt(), anyInt());
		when(preparedStatement.execute()).thenReturn(true);

		// Call the process method with the exchange.
		rdbmsService.process(exchange);

		// Verify that the correct SQL statement was prepared and executed.
		verify(connection).prepareStatement(
			"INSERT INTO test-category (testHardwareIdName, testTimestampName, test-measurement) VALUES (?, ?, ?)");
		verify(preparedStatement).setString(1, "testHardwareId");
		verify(preparedStatement).setTimestamp(2, Timestamp.from(Instant.parse("2025-02-17T12:00:00.123Z")));
		verify(preparedStatement).setInt(3, 10);
		verify(preparedStatement).execute();
	}

	private EsthesisDataMessage createEsthesisDataMessage() {
		return new EsthesisDataMessage(
			"id123",
			"correlationId456",
			"testHardwareId",
			"testComponent",
			"2025-02-17T12:00:00.123Z",
			MessageTypeEnum.T,
			"testChannel",
			new esthesis.common.avro.PayloadData(
				"test-category",
				"2025-02-17T12:00:00.123Z",
				List.of(new ValueData("test-measurement", "10", ValueTypeEnum.INTEGER))));
	}

}
