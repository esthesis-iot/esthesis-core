package esthesis.dataflows.influxdbwriter.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.avro.PayloadData;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(influxDBClient.getWriteApiBlocking()).thenReturn(writeApi);
		when(exchange.getIn()).thenReturn(message);
	}

	@Test
	void testProcess() {
		// Create and mock valid EsthesisDataMessage instance message.
		Instant testTimestamp = Instant.from(Instant.parse("2025-02-13T09:00:00.000Z"));

		EsthesisDataMessage esthesisMessage =
			createEsthesisDataMessage(
				"test-category",
				"hardware-id",
			 Instant.now().toString(),
			"test-name",
				"10.1",
				ValueTypeEnum.DOUBLE,
				testTimestamp.toString());

		when(message.getBody(EsthesisDataMessage.class)).thenReturn(esthesisMessage);

		// Process the exchange.
		influxDBService.process(exchange);

		// Capture the Point object passed to the writePoint method.
		ArgumentCaptor<Point> pointCaptor = ArgumentCaptor.forClass(Point.class);
		verify(writeApi).writePoint(pointCaptor.capture());

		// Assert the point contains the expected data.
		String expectedTimestampInNs = String.valueOf(testTimestamp.toEpochMilli() * 1_000_000);
		assertEquals("test-category,hardwareId=hardware-id test-name=10.1 " + expectedTimestampInNs,
			pointCaptor.getValue().toLineProtocol());
	}

	@Test
	void testProcess_WithoutSeemAt() {
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

}
