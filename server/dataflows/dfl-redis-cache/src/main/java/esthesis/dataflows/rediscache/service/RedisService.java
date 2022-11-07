package esthesis.dataflows.rediscache.service;

import esthesis.dataflow.common.DflUtils;
import esthesis.dataflow.common.parser.EsthesisMessage;
import esthesis.dataflows.rediscache.config.AppConfig;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@ApplicationScoped
public class RedisService {

  @Inject
  RedisDataSource redis;

  @Inject
  AppConfig conf;

  public static final String TIMESTAMP_FIELD_NAME = "timestamp";
  public static final String VALUE_TYPE_FIELD_NAME = "valueType";

  private HashCommands<String, String, String> hashCommand;

  @PostConstruct
  void init() {
    hashCommand = redis.hash(String.class);
  }

  public void process(Exchange exchange) {
    // Get the message from the exchange.
    EsthesisMessage esthesisMessage = exchange.getIn()
        .getBody(EsthesisMessage.class);

    // Write the key to Redis.
    esthesisMessage.getPayload().getValues().forEach((keyValue) -> {
      String key = esthesisMessage.getHardwareId();
      String fieldName = String.join(".",
          esthesisMessage.getPayload().getCategory(), keyValue.getName());
      String fieldValue = keyValue.getValue();
      String fieldType = keyValue.getValueType();

      if (fieldValue.length() <= conf.redisMaxSize()) {
        hashCommand.hset(key, fieldName, fieldValue);
        hashCommand.hset(key, String.join(".", fieldName, TIMESTAMP_FIELD_NAME),
            esthesisMessage.getPayload().getTimestamp());
        hashCommand.hset(key, String.join(".", fieldName,
            VALUE_TYPE_FIELD_NAME), fieldType);
      } else {
        log.debug("Value '{}' for '{}' too long, skipping. Current maximum"
                + " value size is '{}' bytes.",
            StringUtils.abbreviate(fieldValue,
                DflUtils.MESSAGE_LOG_ABBREVIATION_LENGTH), key,
            conf.redisMaxSize());
      }
    });
  }
}
