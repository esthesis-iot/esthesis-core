package esthesis.util.redis;

import static esthesis.common.AppConstants.REDIS_KEY_SUFFIX_TIMESTAMP;
import static esthesis.common.AppConstants.REDIS_KEY_SUFFIX_VALUE_TYPE;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

/**
 * A wrapper around the Jedis client.
 */
@Slf4j
@ApplicationScoped
public class EsthesisRedis {

  private JedisPool jedisPool;
  private Optional<RedisLocationDTO> redisLocationDTO;

  @Inject
  RedisClientLocator redisClientLocator;

  @PostConstruct
  void init() {
    //TODO we need a kafka-based event emission when dfl for redis changes,
    // so that the pool can be reinitialized dynamically.
    redisLocationDTO = redisClientLocator.getRedisLocation();
    redisLocationDTO.ifPresent(redis -> {
      jedisPool = new JedisPool(redis.getHost(),
          redis.getPort(), null, redis.getPassword());
    });
  }

  @PreDestroy
  void destroy() {
    jedisPool.close();
  }

  /**
   * Returns the pool associated with this client. This is useful when you need
   * to perform a Redis operation not supported by this client.
   */
  public JedisPool getJedisPool() {
    return jedisPool;
  }

  /**
   * Retusn the database index associated with the underlying connection of this
   * client.
   */
  public int getDbIndex() {
    return redisLocationDTO.map(RedisLocationDTO::getDbIndex).orElseThrow();
  }

  /**
   * Finds all available unique Redis fields (representing measurements).
   */
  public List<String> findAllUniqueMeasurementNames() {
    try (Jedis jedis = getJedisPool().getResource()) {
      jedis.select(getDbIndex());

      // Get all available keys.
      ScanParams scanParams = new ScanParams();
      scanParams.match("*");
      String cur = ScanParams.SCAN_POINTER_START;
      List<String> keys = new ArrayList<>();
      boolean cycleIsFinished = false;
      while (!cycleIsFinished) {
        ScanResult<String> scanResult = jedis.scan(cur, scanParams);
        cur = scanResult.getCursor();
        for (String key : scanResult.getResult()) {
          keys.add(key);
        }
        if (cur.equals("0")) {
          cycleIsFinished = true;
        }
      }

      // For each key, find the fields.
      Set<String> fields = new TreeSet<>();
      for (String key : keys) {
        jedis.hgetAll(key).forEach((field, value) -> {
          if (!(field.endsWith("valueType") || field.endsWith("timestamp"))) {
            fields.add(field);
          }
        });
      }

      return fields.stream().toList();
    }
  }

  /**
   * Returns the value of the specified fields for the specified key.
   *
   * @param key   The key (hash) to look for.
   * @param field The list of fields to look for.
   */
  public List<String> getValue(String key, List<String> field) {
    try (Jedis jedis = getJedisPool().getResource()) {
      jedis.select(getDbIndex());
      return jedis.hmget(key, field.toArray(new String[0]));
    }
  }

  /**
   * Returns the value of the specified field for the specified key.
   *
   * @param key   The key (hash) to look for.
   * @param field The field to look for.
   */
  public String getValue(String key, String field) {
    try (Jedis jedis = getJedisPool().getResource()) {
      jedis.select(getDbIndex());
      return jedis.hget(key, field);
    }
  }

  /**
   * Returns the last updated timestamp of the specified field for the specified
   * key.
   *
   * @param key   The key (hash) to look for.
   * @param field The field to look for.
   * @return Returns the last updated timestamp or null if the field does not
   * have a timestamp entry associated with.
   */
  public Instant getLastUpdate(String key, String field) {
    try (Jedis jedis = getJedisPool().getResource()) {
      jedis.select(getDbIndex());
      String val = jedis.hget(key, field + "." + REDIS_KEY_SUFFIX_TIMESTAMP);
      if (val == null) {
        log.warn("Could not find timestamp for key '{}' field '{}'.", key,
            field);
        return null;
      } else {
        return Instant.parse(val);
      }
    }
  }

  /**
   * Returns the value type of the specified field for the specified key.
   *
   * @param key   The key (hash) to look for.
   * @param field The field to look for.
   * @return Returns the value type or null if the field does not have a value
   * type entry associated with.
   */
  public String getValueType(String key, String field) {
    try (Jedis jedis = getJedisPool().getResource()) {
      jedis.select(getDbIndex());
      String val = jedis.hget(key, field + "." + REDIS_KEY_SUFFIX_VALUE_TYPE);
      if (val == null) {
        log.warn("Could not find value type for key '{}' field '{}'.", key,
            field);
        return null;
      } else {
        return val;
      }
    }
  }

  /**
   * Returns all fields and their values for the specified key.
   *
   * @param key The key (hash) to look for.
   */
  public List<Triple<String, String, Instant>> getAllForKey(String key) {
    try (Jedis jedis = getJedisPool().getResource()) {
      jedis.select(getDbIndex());

      // Get all fields for the given key.
      Map<String, String> keyValMap = jedis.hgetAll(key);

      // Prepare a list holding the keys, values, and last updated values.
      List<Triple<String, String, Instant>> triples =
          new ArrayList<>(keyValMap.size());

      // Add the key, value, and last updated values to the list.
      keyValMap.entrySet().stream()
          .filter(entry -> !entry.getKey().endsWith(REDIS_KEY_SUFFIX_VALUE_TYPE)
              && !entry.getKey().endsWith(REDIS_KEY_SUFFIX_TIMESTAMP))
          .forEach(entry -> {
            triples.add(new ImmutableTriple(entry.getKey(), entry.getValue(),
                getLastUpdate(key, entry.getKey())));
          });

      return triples;
    }
  }
}
