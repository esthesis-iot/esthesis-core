package esthesis.util.redis;

import static esthesis.core.common.AppConstants.REDIS_KEY_SUFFIX_TIMESTAMP;
import static esthesis.core.common.AppConstants.REDIS_KEY_SUFFIX_VALUE_TYPE;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class RedisUtils {

	private final RedisDataSource redis;
	private final EventBus bus;

	private HashCommands<String, String, String> hashCommandText;
	private KeyCommands<String> keyCommand;
	private ValueCommands<String, Long> countCommands;

	public enum KeyType {
		// Esthesis Device Measurement. Keep the latest value of each measurement for each device.
		ESTHESIS_DM,
		// Esthesis Provisioning Package (download token).
		ESTHESIS_PPDT,
		// Provisioning Request Timer.
		ESTHESIS_PRT,
		// Dataflows report interval.
		ESTHESIS_DFLRI
	}

	@PostConstruct
	void init() {
		hashCommandText = redis.hash(String.class);
		keyCommand = redis.key();
		countCommands = redis.value(Long.class);
	}

	/**
	 * Sets a field into a hash.
	 *
	 * @param keyType The type of this key (the key name will be prefixed with this value).
	 * @param key     The key (name) of the hash.
	 * @param field   The name of the field to set inside the hash.
	 * @param value   the value of the key to set.
	 */
	public void setToHash(KeyType keyType, String key, String field, String value) {
		hashCommandText.hset(keyType + "." + key, field, value);
	}

	/**
	 * Finds the value of a field inside a hash.
	 *
	 * @param keyType The type of this key (the key name will be prefixed with this value).
	 * @param key     The key (name) of the hash.
	 * @param field   The name of the field to find inside the hash.
	 * @return Returns The value of the field.
	 */
	public String getFromHash(KeyType keyType, String key, String field) {
		return hashCommandText.hget(keyType + "." + key, field);
	}

	/**
	 * Finds the value of a field inside a hash. Reactive version.
	 *
	 * @param keyType The type of this key (the key name will be prefixed with this value).
	 * @param key     The key (name) of the hash.
	 * @param field   The name of the field to find inside the hash.
	 * @return Returns The value of the field.
	 */
	public Uni<String> getFromHashReactive(KeyType keyType, String key, String field) {
		return redis.getReactive().hash(String.class).hget(keyType + "." + key, field);
	}

	/**
	 * Returns all fields and their values for a given hash.
	 *
	 * @param keyType The type of this key (the key name will be prefixed with this value).
	 * @param key     The key (name) of the hash.
	 * @return Returns a map of all fields and their values.
	 */
	public Map<String, String> getHash(KeyType keyType, String key) {
		return hashCommandText.hgetall(keyType + "." + key);
	}

	/**
	 * Returns all fields and their values for a given hash.
	 *
	 * @param key The key (name) of the hash.
	 * @return Returns a map of all fields and their values.
	 */
	public Map<String, String> getHash(String key) {
		return hashCommandText.hgetall(key);
	}

	/**
	 * This is a special variation of getHash(String key) that returns a list of triples, where each
	 * triplet contains the field name, the value, and the timestamp of the value.
	 *
	 * @param key The key (name) of the hash.
	 * @return Returns a list of triples.
	 */
	public List<Triple<String, String, Instant>> getHashTriplets(KeyType keyType, String key) {
		// Get all fields for the given key.
		Map<String, String> keyValMap = getHash(keyType, key);

		// Prepare a list holding the keys, values, and last updated values to be returned.
		List<Triple<String, String, Instant>> triplets = new ArrayList<>(keyValMap.size());

		// Add the key, value, and last updated values to the list.
		keyValMap.entrySet().stream().filter(
			entry -> !entry.getKey().endsWith(REDIS_KEY_SUFFIX_VALUE_TYPE) && !entry.getKey()
				.endsWith(REDIS_KEY_SUFFIX_TIMESTAMP)).forEach(entry -> triplets.add(
			new ImmutableTriple<>(entry.getKey(), entry.getValue(),
				getLastUpdate(keyType, key, entry.getKey()))));

		return triplets;
	}

	/**
	 * Sets a hash to expire after a specific number of seconds.
	 *
	 * @param keyType The type of this key (the key name will be prefixed with this value).
	 * @param key     The key (name) of the hash.
	 * @param seconds The number of seconds after which the hash will expire.
	 * @return Returns true if the key was set to expire.
	 */
	public boolean setExpirationForHash(KeyType keyType, String key, long seconds) {
		return keyCommand.expire(keyType + "." + key, seconds);
	}

	/**
	 * Returns the last update date for a specific field inside a hash.
	 * <p>
	 * Entries set in hash by dfl-redis are associated with two additional entries:
	 * <li>
	 * <ul>field.REDIS_KEY_SUFFIX_VALUE_TYPE, indicating the data type of this value</ul>
	 * <ul>field.REDIS_KEY_SUFFIX_TIMESTAMP, indicating the date at which this entry was last updated.</ul>
	 * </li>
	 *
	 * @param keyType The type of this key (the key name will be prefixed with this value).
	 * @param key     The key (name) of the hash.
	 * @param field   The name of the field to find inside the hash.
	 * @return Returns the last update date for the field or null if the field
	 * .REDIS_KEY_SUFFIX_TIMESTAMP entry can not be found.
	 */
	public Instant getLastUpdate(KeyType keyType, String key, String field) {
		String val = hashCommandText.hget(keyType + "." + key,
			field + "." + REDIS_KEY_SUFFIX_TIMESTAMP);
		if (StringUtils.isEmpty(val)) {
			log.trace("Could not find last update for key '{}' field '{}'.", key, field);
			return null;
		} else {
			return Instant.parse(val);
		}
	}

	public List<String> findKeysStartingWith(String prefix) {
		return keyCommand.keys(prefix + "*");
	}

	public boolean isKeysStartingWith(String prefix) {
		return !keyCommand.keys(prefix + "*").isEmpty();
	}

	public boolean keyExists(KeyType keyType, String key) {
		return keyCommand.exists(keyType + "." + key);
	}

	public long incrCounter(KeyType keyType, String key, long expireInSeconds) {
		long val = countCommands.incrby(keyType + "." + key, 1);
		if (expireInSeconds > 0) {
			keyCommand.expire(keyType + "." + key, Duration.ofSeconds(expireInSeconds));
		}

		return val;
	}

	public void resetCounter(KeyType keyType, String key) {
		countCommands.set(keyType + "." + key, 0L);
	}

}

