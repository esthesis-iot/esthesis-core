package esthesis.services.application.impl.service;

import esthesis.service.application.dto.DTValueReply;
import esthesis.service.dataflow.dto.Dataflow;
import esthesis.service.dataflow.resource.DataflowSystemResource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
@ApplicationScoped
public class DTService {

  private final static String VALUE_TYPE_SUFFIX = "valueType";
  private final static String TIMESTAMP_SUFFIX = "timestamp";
  private JedisPool jedisPool;
  private int dbIndex;

  @Inject
  @RestClient
  DataflowSystemResource dataflowSystemResource;

  // Redis server URL is taken from the configuration of Dataflows, so the
  // resulting URL is pointing to the internal Kubernetes cluster IP. To
  // facilitate development, you can set the following property which will
  // override whatever value is discovered in runtime.
  @ConfigProperty(name = "redis.url")
  Optional<String> redisUrl;

  @PostConstruct
  void init() {
    // Find which Redis server is being used.
    List<Dataflow> redisSetup = dataflowSystemResource.getRedisSetup();
    log.info("Redis servers registered: {}", redisSetup.size());
    if (redisSetup.size() > 0) {
      Dataflow redis = redisSetup.get(new Random().nextInt(redisSetup.size()));
      String url = ((Map<String, String>) redis.getConfig().get("redis")).get(
          "url");
      String password = ((Map<String, String>) redis.getConfig()
          .get("redis")).get("password");

      if (redisUrl.isPresent()) {
        url = redisUrl.get();
      }

      Pattern p = Pattern.compile("(redis{1,2})://(.*):(.*)/(\\d*)",
          Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(url);
      boolean matchFound = m.find();
      if (!matchFound) {
        log.error("Redis URL '{}' is not valid, you need to provide a URL in "
            + "the form of redis://localhost:6379/0.", url);
      } else {
        String protocol = m.group(1);
        String host = m.group(2);
        int port = Integer.parseInt(m.group(3));
        dbIndex = Integer.parseInt(m.group(4));

        log.info("User Redis server '{}'.", url);
        jedisPool = new JedisPool(host, port, null, password);
      }
    }
  }

  public DTValueReply find(String hardwareId, String category,
      String measurement) {
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.select(dbIndex);
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
      jedis.select(dbIndex);

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
