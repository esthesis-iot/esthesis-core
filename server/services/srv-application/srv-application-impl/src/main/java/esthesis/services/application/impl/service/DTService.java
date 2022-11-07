package esthesis.services.application.impl.service;

import esthesis.service.application.dto.DTValueReply;
import esthesis.util.redis.EsthesisRedis;
import esthesis.util.redis.RedisLocationDTO;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
@ApplicationScoped
public class DTService {

  private final static String VALUE_TYPE_SUFFIX = "valueType";
  private final static String TIMESTAMP_SUFFIX = "timestamp";
  private JedisPool jedisPool;
  private RedisLocationDTO redisLocationDTO;

  @Inject
  EsthesisRedis esthesisRedis;

  public DTValueReply find(String hardwareId, String category,
      String measurement) {
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.select(redisLocationDTO.getDbIndex());
      String value = jedis.hget(hardwareId,
          String.join(".", category, measurement));
      String valueType = jedis.hget(hardwareId,
          String.join(".", category, measurement, VALUE_TYPE_SUFFIX));
      Instant valueTimestamp = Instant.parse(jedis.hget(hardwareId,
          String.join(".", category, measurement, TIMESTAMP_SUFFIX)));

      return new DTValueReply()
          .setHardwareId(hardwareId)
          .setCategory(category)
          .setMeasurement(measurement)
          .setValueType(valueType)
          .setRecordedAt(valueTimestamp)
          .setValue(value);
    }
  }

  public List<DTValueReply> findAll(String hardwareId, String category) {
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.select(redisLocationDTO.getDbIndex());

      List<DTValueReply> values = new ArrayList<>();
      Map<String, String> keys = jedis.hgetAll(hardwareId);
      keys.forEach((key, value) -> {
        if (key.startsWith(category)) {
          String[] parts = key.split("\\.");
          if (parts.length == 2) {
            values.add(new DTValueReply()
                .setHardwareId(hardwareId)
                .setCategory(category)
                .setMeasurement(parts[1])
                .setValueType(jedis.hget(hardwareId,
                    String.join(".", category, parts[1], VALUE_TYPE_SUFFIX)))
                .setRecordedAt(Instant.parse(jedis.hget(hardwareId,
                    String.join(".", category, parts[1], TIMESTAMP_SUFFIX))))
                .setValue(value));
          }
        }
      });

      return values;
    }
  }
}
