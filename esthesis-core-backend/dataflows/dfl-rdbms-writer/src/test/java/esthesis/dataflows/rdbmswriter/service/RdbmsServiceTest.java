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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
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

	@BeforeEach
	@SneakyThrows
	void setUp() {
		// Initialize mocks.
		MockitoAnnotations.openMocks(this);

		// Set up common mock behaviors.
		when(exchange.getIn()).thenReturn(message);
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

		// Mock PreparedStatement execution methods.
		doNothing().when(preparedStatement).setTimestamp(anyInt(), any(Timestamp.class));


	}

	@Test
	@SneakyThrows
	void process_WithSingleStrategy() {
		// Mock the EsthesisDataMessage to be an integer value.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage(
			new ValueData("test-measurement", "10", ValueTypeEnum.INTEGER)));

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

	@Test
	@SneakyThrows
	void process_WithMultiStrategy_Integer() {
		// Mock common behaviors.
		doNothing().when(preparedStatement).setString(anyInt(), anyString());
		doNothing().when(preparedStatement).setInt(anyInt(), anyInt());
		when(preparedStatement.execute()).thenReturn(true);

		// Mock the EsthesisDataMessage to be an integer value.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage(
			new ValueData("test-measurement", "10", ValueTypeEnum.INTEGER)));

		// Set up the configuration for MULTI storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.MULTI);
		when(config.dbStorageStrategyMultiHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategyMultiTimestampName()).thenReturn("testTimestampName");

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

	@Test
	@SneakyThrows
	void process_WithMultiStrategy_String() {
		// Mock common behaviors.
		doNothing().when(preparedStatement).setString(anyInt(), anyString());
		when(preparedStatement.execute()).thenReturn(true);

		// Mock the EsthesisDataMessage to be a String value.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage(
			new ValueData("test-measurement", "10", ValueTypeEnum.STRING)));

		// Set up the configuration for MULTI storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.MULTI);
		when(config.dbStorageStrategyMultiHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategyMultiTimestampName()).thenReturn("testTimestampName");

		// Call the process method with the exchange.
		rdbmsService.process(exchange);

		// Verify that the correct SQL statement was prepared and executed.
		verify(connection).prepareStatement(
			"INSERT INTO test-category (testHardwareIdName, testTimestampName, test-measurement) VALUES (?, ?, ?)");
		verify(preparedStatement).setString(1, "testHardwareId");
		verify(preparedStatement).setTimestamp(2, Timestamp.from(Instant.parse("2025-02-17T12:00:00.123Z")));
		verify(preparedStatement).setString(3, "10");
		verify(preparedStatement).execute();
	}

	@Test
	@SneakyThrows
	void process_WithMultiStrategy_Boolean() {
		// Mock common behaviors.
		doNothing().when(preparedStatement).setString(anyInt(), anyString());
		doNothing().when(preparedStatement).setBoolean(anyInt(), anyBoolean());
		when(preparedStatement.execute()).thenReturn(true);

		// Mock the EsthesisDataMessage to be a boolean value.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage(
			new ValueData("test-measurement", "true", ValueTypeEnum.BOOLEAN)));

		// Set up the configuration for MULTI storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.MULTI);
		when(config.dbStorageStrategyMultiHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategyMultiTimestampName()).thenReturn("testTimestampName");

		// Call the process method with the exchange.
		rdbmsService.process(exchange);

		// Verify that the correct SQL statement was prepared and executed.
		verify(connection).prepareStatement(
			"INSERT INTO test-category (testHardwareIdName, testTimestampName, test-measurement) VALUES (?, ?, ?)");
		verify(preparedStatement).setString(1, "testHardwareId");
		verify(preparedStatement).setTimestamp(2, Timestamp.from(Instant.parse("2025-02-17T12:00:00.123Z")));
		verify(preparedStatement).setBoolean(3, true);
		verify(preparedStatement).execute();
	}

	@Test
	@SneakyThrows
	void process_WithMultiStrategy_Byte() {
		// Mock common behaviors.
		doNothing().when(preparedStatement).setString(anyInt(), anyString());
		doNothing().when(preparedStatement).setByte(anyInt(), anyByte());
		when(preparedStatement.execute()).thenReturn(true);

		// Mock the EsthesisDataMessage to be a byte value.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage(
			new ValueData("test-measurement", "1", ValueTypeEnum.BYTE)));

		// Set up the configuration for MULTI storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.MULTI);
		when(config.dbStorageStrategyMultiHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategyMultiTimestampName()).thenReturn("testTimestampName");


		// Call the process method with the exchange.
		rdbmsService.process(exchange);

		// Verify that the correct SQL statement was prepared and executed.
		verify(connection).prepareStatement(
			"INSERT INTO test-category (testHardwareIdName, testTimestampName, test-measurement) VALUES (?, ?, ?)");
		verify(preparedStatement).setString(1, "testHardwareId");
		verify(preparedStatement).setTimestamp(2, Timestamp.from(Instant.parse("2025-02-17T12:00:00.123Z")));
		verify(preparedStatement).setByte(3, (byte) 1);
		verify(preparedStatement).execute();
	}

	@Test
	@SneakyThrows
	void process_WithMultiStrategy_Short() {
		// Mock common behaviors.
		doNothing().when(preparedStatement).setString(anyInt(), anyString());
		doNothing().when(preparedStatement).setShort(anyInt(), anyShort());
		when(preparedStatement.execute()).thenReturn(true);

		// Mock the EsthesisDataMessage to be a short value.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage(
			new ValueData("test-measurement", "1", ValueTypeEnum.SHORT)));

		// Set up the configuration for MULTI storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.MULTI);
		when(config.dbStorageStrategyMultiHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategyMultiTimestampName()).thenReturn("testTimestampName");


		// Call the process method with the exchange.
		rdbmsService.process(exchange);

		// Verify that the correct SQL statement was prepared and executed.
		verify(connection).prepareStatement(
			"INSERT INTO test-category (testHardwareIdName, testTimestampName, test-measurement) VALUES (?, ?, ?)");
		verify(preparedStatement).setString(1, "testHardwareId");
		verify(preparedStatement).setTimestamp(2, Timestamp.from(Instant.parse("2025-02-17T12:00:00.123Z")));
		verify(preparedStatement).setShort(3, (short) 1);
		verify(preparedStatement).execute();
	}

	@Test
	@SneakyThrows
	void process_WithMultiStrategy_Long() {
		// Mock common behaviors.
		doNothing().when(preparedStatement).setString(anyInt(), anyString());
		doNothing().when(preparedStatement).setLong(anyInt(), anyLong());
		when(preparedStatement.execute()).thenReturn(true);

		// Mock the EsthesisDataMessage to be a long value.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage(
			new ValueData("test-measurement", "10000000", ValueTypeEnum.LONG)));

		// Set up the configuration for MULTI storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.MULTI);
		when(config.dbStorageStrategyMultiHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategyMultiTimestampName()).thenReturn("testTimestampName");


		// Call the process method with the exchange.
		rdbmsService.process(exchange);

		// Verify that the correct SQL statement was prepared and executed.
		verify(connection).prepareStatement(
			"INSERT INTO test-category (testHardwareIdName, testTimestampName, test-measurement) VALUES (?, ?, ?)");
		verify(preparedStatement).setString(1, "testHardwareId");
		verify(preparedStatement).setTimestamp(2, Timestamp.from(Instant.parse("2025-02-17T12:00:00.123Z")));
		verify(preparedStatement).setLong(3, 10000000L);
		verify(preparedStatement).execute();
	}

	@Test
	@SneakyThrows
	void process_WithMultiStrategy_Float() {
		// Mock common behaviors.
		doNothing().when(preparedStatement).setString(anyInt(), anyString());
		doNothing().when(preparedStatement).setFloat(anyInt(), anyFloat());
		when(preparedStatement.execute()).thenReturn(true);

		// Mock the EsthesisDataMessage to be a float value.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage(
			new ValueData("test-measurement", "10.00", ValueTypeEnum.FLOAT)));

		// Set up the configuration for MULTI storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.MULTI);
		when(config.dbStorageStrategyMultiHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategyMultiTimestampName()).thenReturn("testTimestampName");

		// Call the process method with the exchange.
		rdbmsService.process(exchange);

		// Verify that the correct SQL statement was prepared and executed.
		verify(connection).prepareStatement(
			"INSERT INTO test-category (testHardwareIdName, testTimestampName, test-measurement) VALUES (?, ?, ?)");
		verify(preparedStatement).setString(1, "testHardwareId");
		verify(preparedStatement).setTimestamp(2, Timestamp.from(Instant.parse("2025-02-17T12:00:00.123Z")));
		verify(preparedStatement).setFloat(3, 10.00f);
		verify(preparedStatement).execute();
	}

	@Test
	@SneakyThrows
	void process_WithMultiStrategy_Double() {
		// Mock common behaviors.
		doNothing().when(preparedStatement).setString(anyInt(), anyString());
		doNothing().when(preparedStatement).setDouble(anyInt(), anyDouble());
		when(preparedStatement.execute()).thenReturn(true);

		// Mock the EsthesisDataMessage to be a double value.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage(
			new ValueData("test-measurement", "10.00", ValueTypeEnum.DOUBLE)));

		// Set up the configuration for MULTI storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.MULTI);
		when(config.dbStorageStrategyMultiHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategyMultiTimestampName()).thenReturn("testTimestampName");

		// Call the process method with the exchange.
		rdbmsService.process(exchange);

		// Verify that the correct SQL statement was prepared and executed.
		verify(connection).prepareStatement(
			"INSERT INTO test-category (testHardwareIdName, testTimestampName, test-measurement) VALUES (?, ?, ?)");
		verify(preparedStatement).setString(1, "testHardwareId");
		verify(preparedStatement).setTimestamp(2, Timestamp.from(Instant.parse("2025-02-17T12:00:00.123Z")));
		verify(preparedStatement).setDouble(3, 10.00);
		verify(preparedStatement).execute();
	}

	@Test
	@SneakyThrows
	void process_WithMultiStrategy_BigDecimal() {
		// Mock common behaviors.
		doNothing().when(preparedStatement).setString(anyInt(), anyString());
		doNothing().when(preparedStatement).setBigDecimal(anyInt(), any());
		when(preparedStatement.execute()).thenReturn(true);

		// Mock the EsthesisDataMessage to be a big decimal value.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage(
			new ValueData("test-measurement", "100", ValueTypeEnum.BIG_DECIMAL)));

		// Set up the configuration for MULTI storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.MULTI);
		when(config.dbStorageStrategyMultiHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategyMultiTimestampName()).thenReturn("testTimestampName");

		// Call the process method with the exchange.
		rdbmsService.process(exchange);

		// Verify that the correct SQL statement was prepared and executed.
		verify(connection).prepareStatement(
			"INSERT INTO test-category (testHardwareIdName, testTimestampName, test-measurement) VALUES (?, ?, ?)");
		verify(preparedStatement).setString(1, "testHardwareId");
		verify(preparedStatement).setTimestamp(2, Timestamp.from(Instant.parse("2025-02-17T12:00:00.123Z")));
		verify(preparedStatement).setBigDecimal(3, new BigDecimal(100));
		verify(preparedStatement).execute();
	}

	@Test
	@SneakyThrows
	void process_WithMultiStrategy_Unknown() {
		// Mock common behaviors.
		doNothing().when(preparedStatement).setString(anyInt(), anyString());
		when(preparedStatement.execute()).thenReturn(true);

		// Mock the EsthesisDataMessage to be an unknown value.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage(
			new ValueData("test-measurement", "unknown", ValueTypeEnum.UNKNOWN)));

		// Set up the configuration for MULTI storage strategy.
		when(config.dbStorageStrategy()).thenReturn(STORAGE_STRATEGY.MULTI);
		when(config.dbStorageStrategyMultiHardwareIdName()).thenReturn("testHardwareIdName");
		when(config.dbStorageStrategyMultiTimestampName()).thenReturn("testTimestampName");

		// Call the process method with the exchange.
		rdbmsService.process(exchange);

		// Verify that the correct SQL statement was prepared and executed.
		verify(connection).prepareStatement(
			"INSERT INTO test-category (testHardwareIdName, testTimestampName, test-measurement) VALUES (?, ?, ?)");
		verify(preparedStatement).setString(1, "testHardwareId");
		verify(preparedStatement).setTimestamp(2, Timestamp.from(Instant.parse("2025-02-17T12:00:00.123Z")));
		verify(preparedStatement).setString(3, "unknown");
		verify(preparedStatement).execute();
	}

	private EsthesisDataMessage createEsthesisDataMessage(ValueData valueData) {
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
				List.of(valueData)));
	}

}
