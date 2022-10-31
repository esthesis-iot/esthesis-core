package esthesis.dataflows.influxdbwriter.service;

import esthesis.dataflow.common.parser.EsthesisMessage;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;

@Slf4j
@ApplicationScoped
public class RedisService {

  @Inject
  RedisDataSource redis;

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

      hashCommand.hset(key, fieldName, fieldValue);
      hashCommand.hset(key, String.join(".", fieldName, TIMESTAMP_FIELD_NAME),
          esthesisMessage.getPayload().getTimestamp());
      hashCommand.hset(key, String.join(".", fieldName,
          VALUE_TYPE_FIELD_NAME), fieldType);
    });
  }
}
