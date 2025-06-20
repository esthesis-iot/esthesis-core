package esthesis.util.redis;

import esthesis.util.redis.RedisUtils.KeyType;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class RedisUtilsTest {

	@Inject
	RedisUtils redisUtils;

	@Inject
	RedisDataSource redis;


	@BeforeEach
	void setUp() {
		redis.flushall();
	}

	@Test
	void testSetAndGetFromHash() {
		// Verify that the value is not set before calling setToHash.
		assertNull(redisUtils.getFromHash(KeyType.ESTHESIS_DM, "test-key", "test-field"));

		redisUtils.setToHash(KeyType.ESTHESIS_DM, "test-key", "test-field", "test-value");

		// Verify that the value is set correctly.
		assertEquals("test-value", redisUtils.getFromHash(KeyType.ESTHESIS_DM, "test-key", "test-field"));
	}


	@Test
	void testGetHashAndGetHashTriplets() {
		// Verify that the hash is empty before setting any values.
		assertTrue(redisUtils.getHashTriplets(KeyType.ESTHESIS_DM, "test-key").isEmpty());
		assertTrue(redisUtils.getHash(KeyType.ESTHESIS_DM, "test-key").isEmpty());
		assertTrue(redisUtils.getHash(KeyType.ESTHESIS_DM + ".test-key").isEmpty());

		redisUtils.setToHash(KeyType.ESTHESIS_DM, "test-key", "test-field", "test-value");

		// Verify that the hash contains the expected field and value.
		assertTrue(redisUtils.getHash(KeyType.ESTHESIS_DM, "test-key").containsKey("test-field"));
		assertTrue(redisUtils.getHash(KeyType.ESTHESIS_DM + ".test-key").containsKey("test-field"));
		assertEquals("test-value", redisUtils.getHash(KeyType.ESTHESIS_DM, "test-key").get("test-field"));
		assertFalse(redisUtils.getHashTriplets(KeyType.ESTHESIS_DM, "test-key").isEmpty());
		assertEquals("test-field", redisUtils.getHashTriplets(KeyType.ESTHESIS_DM, "test-key").getFirst().getLeft());
	}

	@Test
	void testKeyExists() {
		// Verify that the key does not exist before setting expiration.
		assertFalse(redisUtils.keyExists(KeyType.ESTHESIS_DM, "test-key"));

		redisUtils.setToHash(KeyType.ESTHESIS_DM, "test-key", "test-field", "test-value");

		// Verify that the key exists after setting expiration.
		assertTrue(redisUtils.keyExists(KeyType.ESTHESIS_DM, "test-key"));
	}

	@Test
	void testSetExpirationForHash() {
		redisUtils.setToHash(KeyType.ESTHESIS_DM, "test-key", "test-field", "test-value");
		assertTrue(redisUtils.setExpirationForHash(KeyType.ESTHESIS_DM, "test-key", 2));

		// Verify that the key exists after setting expiration.
		assertTrue(redisUtils.keyExists(KeyType.ESTHESIS_DM, "test-key"));

		// Wait for the expiration time to pass.
		try {
			Thread.sleep(2100); // Sleep for 3 seconds to ensure the key expires.
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		// Verify that the key no longer exists after expiration.
		assertFalse(redisUtils.keyExists(KeyType.ESTHESIS_DM, "test-key"));
	}

	@Test
	void testfindKeysStartingWith() {
		redisUtils.setToHash(KeyType.ESTHESIS_DM, "test-key", "test-field", "test-value");
		assertFalse(redisUtils.findKeysStartingWith(KeyType.ESTHESIS_DM + ".").isEmpty());
		assertTrue(redisUtils.findKeysStartingWith("unexistent.").isEmpty());
	}

	@Test
	void testisKeysStartingWith() {
		redisUtils.setToHash(KeyType.ESTHESIS_DM, "test-key", "test-field", "test-value");
		assertTrue(redisUtils.isKeysStartingWith(KeyType.ESTHESIS_DM + "."));
		assertFalse(redisUtils.isKeysStartingWith("unexistent."));
	}

	@ParameterizedTest
	@ValueSource(ints = {0, 5, 10})
	void testIncrCounterAndResetCounter(int expireInSeconds) {
		long count1 = redisUtils.incrCounter(KeyType.ESTHESIS_DM, "test-key", expireInSeconds);
		assertEquals(1, count1);

		long count2 = redisUtils.incrCounter(KeyType.ESTHESIS_DM, "test-key", expireInSeconds);
		assertEquals(2, count2);

		redisUtils.resetCounter(KeyType.ESTHESIS_DM, "test-key");

		long countAfterReset = redisUtils.incrCounter(KeyType.ESTHESIS_DM, "test-key", expireInSeconds);
		assertEquals(1, countAfterReset);
	}

	@Test
	void testGetLastUpdateReturnsNullIfNoTimestamp() {
		redisUtils.setToHash(KeyType.ESTHESIS_DM, "test-key", "test-field", "test-value");

		Instant lastUpdate = redisUtils.getLastUpdate(KeyType.ESTHESIS_DM, "test-key", "test-field");
		assertNull(lastUpdate);
	}


}
