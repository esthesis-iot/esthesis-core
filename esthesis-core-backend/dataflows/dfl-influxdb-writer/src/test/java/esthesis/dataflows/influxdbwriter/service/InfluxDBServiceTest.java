package esthesis.dataflows.influxdbwriter.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.avro.PayloadData;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import esthesis.dataflows.influxdbwriter.config.AppConfig;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InfluxDBServiceTest {

	@Mock
	private InfluxDBClient influxDBClient;

	@Mock
	private WriteApiBlocking writeApi;

	@Mock
	private Exchange exchange;

	@Mock
	private Message message;

	@InjectMocks
	private InfluxDBService influxDBService;

	@Mock
	AppConfig config;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@ParameterizedTest
	@MethodSource("valueTypeProvider")
	void testProcess_WithAllValueTypes(
		String testName,
		ValueTypeEnum valueTypeEnum,
		String measurementValue,
		Object expectedFieldValue,
		String expectedFieldString
	) {
		// Mock the InfluxDB client and exchange.
		when(influxDBClient.getWriteApiBlocking()).thenReturn(writeApi);
		when(exchange.getIn()).thenReturn(message);

		// Create and mock valid EsthesisDataMessage instance message.
		String testCategory = "category-" + testName;
		String hardwareId = "hw-" + testName;
		String measurementName = "field-" + testName;
		Instant timestamp = Instant.parse("2025-02-13T09:00:00.000Z");

		EsthesisDataMessage esthesisMessage = createEsthesisDataMessage(
			testCategory,
			hardwareId,
			null,
			measurementName,
			measurementValue,
			valueTypeEnum,
			timestamp.toString()
		);
		// Mock the message body to return the EsthesisDataMessage.
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(esthesisMessage);

		// Process the exchange.
		influxDBService.process(exchange);

		// Capture the Point object passed to the writePoint method.
		ArgumentCaptor<Point> pointCaptor = ArgumentCaptor.forClass(Point.class);
		verify(writeApi).writePoint(pointCaptor.capture());

		String actualLineProtocol = pointCaptor.getValue().toLineProtocol();
		String expectedTimestampInNs = String.valueOf(timestamp.toEpochMilli() * 1_000_000);

		// Assert the point contains the expected data.
		assertTrue(actualLineProtocol.contains(testCategory));
		assertTrue(actualLineProtocol.contains("hardwareId=" + hardwareId));
		assertTrue(actualLineProtocol.contains(measurementName + "=" + expectedFieldString));
		assertTrue(actualLineProtocol.endsWith(expectedTimestampInNs));
		assertTrue(actualLineProtocol.contains(expectedFieldValue.toString()));
	}

	@Test
	void testProcess_WithoutSeemAt() {
		// Mock the InfluxDB client and exchange.
		when(influxDBClient.getWriteApiBlocking()).thenReturn(writeApi);
		when(exchange.getIn()).thenReturn(message);

		// Create and mock valid EsthesisDataMessage instance message.
		Instant testTimestamp = Instant.from(Instant.parse("2025-02-13T09:00:00.000Z"));

		EsthesisDataMessage esthesisMessage =
			createEsthesisDataMessage(
				"test-category-2",
				"hardware-id-2",
				null,
				"test-name-2",
				"text-value",
				ValueTypeEnum.STRING,
				testTimestamp.toString());

		when(message.getBody(EsthesisDataMessage.class)).thenReturn(esthesisMessage);

		// Process the exchange.
		influxDBService.process(exchange);

		// Capture the Point object passed to the writePoint method.
		ArgumentCaptor<Point> pointCaptor = ArgumentCaptor.forClass(Point.class);
		verify(writeApi).writePoint(pointCaptor.capture());

		// Assert the point contains the expected data.
		String expectedTimestampInNs = String.valueOf(testTimestamp.toEpochMilli() * 1_000_000);
		assertEquals("test-category-2,hardwareId=hardware-id-2 test-name-2=\"text-value\" " + expectedTimestampInNs,
			pointCaptor.getValue().toLineProtocol());
	}

	@Test
	void testReconnectClosesOldClient() {
		// Assign a pre-existing client.
		influxDBService.influxDBClient = influxDBClient;

		when(config.influxUrl()).thenReturn("http://localhost");
		when(config.influxToken()).thenReturn("test-token");
		when(config.influxOrg()).thenReturn("org");
		when(config.influxBucket()).thenReturn("bucket");

		try (MockedStatic<InfluxDBClientFactory> influxDBClientFactoryMockedStatic =
					 mockStatic(InfluxDBClientFactory.class)) {
			influxDBClientFactoryMockedStatic
				.when(() -> InfluxDBClientFactory.create(
					anyString(), any(char[].class), anyString(), anyString()))
				.thenReturn(influxDBClient);

			influxDBService.init();

			// Verify that the old client is closed and a new client is created.
			verify(influxDBClient).close();
			influxDBClientFactoryMockedStatic.verify(() ->
				InfluxDBClientFactory.create(
					"http://localhost", "test-token".toCharArray(), "org", "bucket")
			);
		}
	}

	private EsthesisDataMessage createEsthesisDataMessage(
		String category,
		String hardwareId,
		String seenAt,
		String measurementName,
		String measurementValue,
		ValueTypeEnum measurementValueType,
		String measurementTimestamp) {
		return new EsthesisDataMessage(
			"test-id",
			"correlation-id",
			hardwareId,
			"seenby-test",
			seenAt,
			MessageTypeEnum.T,
			"test-channel",
			new PayloadData(
				category,
				measurementTimestamp,
				List.of(
					ValueData.newBuilder()
						.setName(measurementName)
						.setValue(measurementValue)
						.setValueType(measurementValueType)
						.build())
			));
	}

	static Stream<Arguments> valueTypeProvider() {
		return Stream.of(
			Arguments.of("STRING", ValueTypeEnum.STRING, "abc", "abc", "\"abc\""),
			Arguments.of("BIG_INTEGER", ValueTypeEnum.BIG_INTEGER, "1234567890123456789", "1234567890123456789", "\"1234567890123456789\""),
			Arguments.of("BIG_DECIMAL", ValueTypeEnum.BIG_DECIMAL, "12.345", "12.345", "\"12.345\""),
			Arguments.of("BOOLEAN_true", ValueTypeEnum.BOOLEAN, "true", true, "true"),
			Arguments.of("BOOLEAN_false", ValueTypeEnum.BOOLEAN, "false", false, "false"),
			Arguments.of("BYTE", ValueTypeEnum.BYTE, "42", (byte) 42, "42"),
			Arguments.of("SHORT", ValueTypeEnum.SHORT, "123", (short) 123, "123"),
			Arguments.of("INTEGER", ValueTypeEnum.INTEGER, "456", 456, "456"),
			Arguments.of("LONG", ValueTypeEnum.LONG, "123456789", 123456789L, "123456789"),
			Arguments.of("FLOAT", ValueTypeEnum.FLOAT, "3.14", 3.14f, "3.14"),
			Arguments.of("DOUBLE", ValueTypeEnum.DOUBLE, "2.718", 2.718d, "2.718")
		);
	}

}
