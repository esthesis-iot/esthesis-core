package esthesis.dataflows.pingupdater.service;

import com.mongodb.client.model.Filters;
import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PingServiceTest {

	@Mock
	private Exchange exchange;

	@Mock
	private Message message;


	@InjectMocks
	PingService pingService;

	@BeforeEach
	void setUp() {
		// Initialize  and setup mocks.
		MockitoAnnotations.openMocks(this);
		when(exchange.getIn()).thenReturn(message);
		when(message.getBody(EsthesisDataMessage.class))
			.thenReturn(createPingEsthesisDataMessage());
	}

	@Test
	void searchForExistingDevice() {
		// Perform the searchForExistingDevice method execution.
		pingService.searchForExistingDevice(exchange);

		// Create the expected Filter object.
		Bson expectedFilter = Filters.eq("hardwareId", "testHardwareId");

		// Verify the setHeader call with the Filter object.
		verify(message).setHeader("CamelMongoDbCriteria", expectedFilter);

	}


	@Test
	void updateTimestamp() {
		pingService.updateTimestamp(exchange);

		// Create the expected BsonDocument.
		BsonDocument expectedUpdate = new BsonDocument().append("$set",
			new BsonDocument("lastSeen",
				new BsonDateTime(Instant.parse("2025-02-17T12:00:00.123Z").toEpochMilli())));

		// Verify the setBody call with the BsonDocument.
		verify(message).setBody(expectedUpdate);
	}


	private EsthesisDataMessage createPingEsthesisDataMessage() {
		return new EsthesisDataMessage(
			"id123",
			"correlationId456",
			"testHardwareId",
			"testComponent",
			"2025-02-12T12:00:00.123Z",
			MessageTypeEnum.P,
			"testChannel",
			new esthesis.common.avro.PayloadData(
				"ping",
				"2025-02-17T12:00:00.123Z",
				List.of(new ValueData("ping", "2025-02-17T12:00:00.123Z", ValueTypeEnum.STRING))));
	}
}
