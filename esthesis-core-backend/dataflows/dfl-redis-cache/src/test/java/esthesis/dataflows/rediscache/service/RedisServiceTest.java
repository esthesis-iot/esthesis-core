package esthesis.dataflows.rediscache.service;

import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import esthesis.dataflows.rediscache.config.AppConfig;
import esthesis.util.redis.RedisUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

	@Mock
	private AppConfig config;

	@Mock
	private Exchange exchange;

	@Mock
	private Message message;

	@Mock
	private RedisUtils redisUtils;

	@InjectMocks
	RedisService redisService;

	@BeforeEach
	void setUp() {
		// Initialize mocks.
		MockitoAnnotations.openMocks(this);

		// Set up common mock behaviors.
		when(exchange.getIn()).thenReturn(message);
		when(message.getBody(EsthesisDataMessage.class)).thenReturn(createEsthesisDataMessage());


	}

	@Test
	void process_WithHighMaxSizeAndTTL() {
		// Mock the RedisUtils calls.
		doNothing().when(redisUtils).setToHash(any(RedisUtils.KeyType.class), anyString(), anyString(), anyString());

		// Mock the config for TTL and MaxSize.
		when(config.redisTtl()).thenReturn(60L);
		when(config.redisMaxSize()).thenReturn(1024 * 1024);

		// Process the message.
		redisService.process(exchange);

		// Verify the calls to Redis have been made with the correct parameters.
		verify(redisUtils)
			.setToHash(
				RedisUtils.KeyType.ESTHESIS_DM,
				"testHardwareId",
				"test-category.test-measurement",
				"10");

		verify(redisUtils)
			.setToHash(
				RedisUtils.KeyType.ESTHESIS_DM,
				"testHardwareId",
				"test-category.test-measurement.timestamp",
				"2025-02-18T12:00:00.123Z");

		verify(redisUtils)
			.setToHash(
				RedisUtils.KeyType.ESTHESIS_DM,
				"testHardwareId",
				"test-category.test-measurement.valueType",
				"INTEGER");

		verify(redisUtils).setExpirationForHash(RedisUtils.KeyType.ESTHESIS_DM, "testHardwareId", (60 * 60));

	}

	@Test
	void process_WithLowMaxSize() {
		// Mock the config for no TTL and a very low MaxSize.
		when(config.redisTtl()).thenReturn(0L);
		when(config.redisMaxSize()).thenReturn(1);

		// Process the message.
		redisService.process(exchange);

		// Verify the calls to Redis have not been made.
		verify(redisUtils, times(0))
			.setToHash(any(RedisUtils.KeyType.class), anyString(), anyString(), anyString());

		verify(redisUtils, times(0))
			.setExpirationForHash(any(RedisUtils.KeyType.class), anyString(), anyLong());


	}

	private EsthesisDataMessage createEsthesisDataMessage() {
		return new EsthesisDataMessage(
			"id123",
			"correlationId456",
			"testHardwareId",
			"testComponent",
			"2025-02-18T12:00:00.123Z",
			MessageTypeEnum.T,
			"testChannel",
			new esthesis.common.avro.PayloadData(
				"test-category",
				"2025-02-18T12:00:00.123Z",
				List.of(new ValueData("test-measurement", "10", ValueTypeEnum.INTEGER))));
	}
}
