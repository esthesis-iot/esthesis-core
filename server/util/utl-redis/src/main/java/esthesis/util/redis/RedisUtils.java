package esthesis.util.redis;

import static esthesis.common.AppConstants.REDIS_KEY_SUFFIX_TIMESTAMP;
import static esthesis.common.AppConstants.REDIS_KEY_SUFFIX_VALUE_TYPE;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.bson.types.ObjectId;

@Slf4j
@ApplicationScoped
public class RedisUtils {

  @Inject
  RedisDataSource redis;

  private final static String KEY_PREFIX = "esthesis.";
  private HashCommands<String, String, String> hashCommandText;
  private HashCommands<String, String, byte[]> hashCommandBinary;
  private KeyCommands<String> keyCommand;

  public enum KeyType {
    ESTHESIS_DM,  // Esthesis Device Measurement
    ESTHESIS_PP   // Esthesis Provisioning Package
  }

  @PostConstruct
  void init() {
    hashCommandText = redis.hash(String.class);
    hashCommandBinary = redis.hash(byte[].class);
    keyCommand = redis.key();
  }

  /**
   * Sets a field into a hash.
   *
   * @param keyType The type of this key (the key name will be prefixed with this value).
   * @param key     The key (name) of the hash.
   * @param field   The name of the field to set inside the hash.
   * @param value   the value of the key to set.
   * @return Returns true if the value was set.
   */
  public boolean setToHash(KeyType keyType, String key, String field, String value) {
    return hashCommandText.hset(keyType + "." + key, field, value);
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
            .endsWith(REDIS_KEY_SUFFIX_TIMESTAMP)).forEach(entry -> {
      triplets.add(new ImmutableTriple<>(entry.getKey(), entry.getValue(),
          getLastUpdate(keyType, key, entry.getKey())));
    });

    return triplets;
  }

  /**
   * Sets a hash to expire after a specific number of seconds. Note that Redis does not allow to
   * individually expire fields within a hash.
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
      log.debug("Could not find last update for key '{}' field '{}'.", key, field);
      return null;
    } else {
      return Instant.parse(val);
    }
  }

  public List<String> findKeysStartingWith(String prefix) {
    return keyCommand.keys(prefix + "*");
  }

  public boolean cacheProvisioningPackage(String packageId, byte[] file) {
    return
        hashCommandBinary.hset(KeyType.ESTHESIS_PP + "." + packageId, "file", file) &&
            hashCommandText.hset(KeyType.ESTHESIS_PP + "." + packageId,
                "file." + REDIS_KEY_SUFFIX_TIMESTAMP, Instant.now().toString());
  }

  public void deleteProvisioningPackage(ObjectId provisioningPackageId) {
    keyCommand.del(KeyType.ESTHESIS_PP + "." + provisioningPackageId.toString());
  }

  public boolean keyExists(KeyType keyType, String key) {
    return keyCommand.exists(keyType + "." + key);
  }

  public void downloadProvisioningPackage(ObjectId provisioningPackageId, OutputStream outputStream) {
    hashCommandBinary.
    byte[] file = hashCommandBinary.hget(KeyType.ESTHESIS_PP + "." + provisioningPackageId.toString(), "file");
    try {
      outputStream.write(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
