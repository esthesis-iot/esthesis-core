package esthesis.dataflows.rediscache.service;

import esthesis.avro.EsthesisDataMessage;
import esthesis.dataflow.common.DflUtils;
import esthesis.dataflows.rediscache.config.AppConfig;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@ApplicationScoped
public class RedisService {

  @Inject
  AppConfig conf;

  @Inject
  RedisUtils redisUtils;

  public static final String TIMESTAMP_FIELD_NAME = "timestamp";
  public static final String VALUE_TYPE_FIELD_NAME = "valueType";

  public void process(Exchange exchange) {
    // Get the message from the exchange.
    EsthesisDataMessage esthesisMessage = exchange.getIn()
        .getBody(EsthesisDataMessage.class);

    // Write the key to Redis.
    esthesisMessage.getPayload().getValues().forEach((keyValue) -> {
      String key = esthesisMessage.getHardwareId();
      String fieldName = String.join(".",
          esthesisMessage.getPayload().getCategory(), keyValue.getName());
      String fieldValue = keyValue.getValue();
      String fieldType = keyValue.getValueType();

      if (fieldValue.length() <= conf.redisMaxSize()) {
        redisUtils.setToHash(KeyType.ESTHESIS_DM, key, fieldName, fieldValue);
        redisUtils.setToHash(KeyType.ESTHESIS_DM, key, String.join(".", fieldName,
                TIMESTAMP_FIELD_NAME),
            esthesisMessage.getPayload().getTimestamp());
        redisUtils.setToHash(KeyType.ESTHESIS_DM, key, String.join(".", fieldName,
            VALUE_TYPE_FIELD_NAME), fieldType);
      } else {
        log.debug("Value '{}' for '{}' too long, skipping. Current maximum"
                + " value size is '{}' bytes.",
            StringUtils.abbreviate(fieldValue,
                DflUtils.MESSAGE_LOG_ABBREVIATION_LENGTH), key,
            conf.redisMaxSize());
      }

      // Expire hash, if requested.
      if (conf.redisTtl() > 0) {
        redisUtils.setExpirationForHash(KeyType.ESTHESIS_DM, key, conf.redisTtl() * 60);
      }
    });
  }
}
