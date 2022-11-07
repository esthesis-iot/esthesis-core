package esthesis.util.redis;

import static esthesis.common.AppConstants.REDIS_KEY_SUFFIX_TIMESTAMP;
import static esthesis.common.AppConstants.REDIS_KEY_SUFFIX_VALUE_TYPE;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

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

  public JedisPool getJedisPool() {
    return jedisPool;
  }

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

  public List<String> getValue(String key, List<String> field) {
    try (Jedis jedis = getJedisPool().getResource()) {
      jedis.select(getDbIndex());
      return jedis.hmget(key, field.toArray(new String[0]));
    }
  }

  public String getValue(String key, String field) {
    try (Jedis jedis = getJedisPool().getResource()) {
      jedis.select(getDbIndex());
      return jedis.hget(key, field);
    }
  }

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
}
