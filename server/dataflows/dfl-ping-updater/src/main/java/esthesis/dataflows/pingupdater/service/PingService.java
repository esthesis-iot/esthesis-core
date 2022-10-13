package esthesis.dataflows.pingupdater.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.dataflow.common.messages.EsthesisMessage;
import java.time.Instant;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;

@ApplicationScoped
public class PingService {

  @Inject
  ObjectMapper objectMapper;

  private final static String PING_PAYLOAD_PREFIX = "$health.ping=";
  private final static String PING_ATTRIBUTE_NAME = "lastSeen";
  private final static String PING_TIMESTAMP_EXCHANGE_PROPERTY = "EsthesisPingTimestamp";
  
  public void extractPingTimestamp(Exchange exchange)
  throws JsonProcessingException {
    EsthesisMessage esthesisMessage = objectMapper.readValue(
        exchange.getIn().getBody(String.class), EsthesisMessage.class);
    String pingPayload =
        StringUtils.substringAfter(esthesisMessage.getPayload(),
            PING_PAYLOAD_PREFIX);
    exchange.setProperty(PING_TIMESTAMP_EXCHANGE_PROPERTY,
        Instant.parse(pingPayload));
  }

  public void updateTimestamp(Exchange exchange) {
    BsonDocument updateObj = new BsonDocument().append("$set",
        new BsonDocument(PING_ATTRIBUTE_NAME,
            new BsonDateTime(exchange.getProperty(
                    PING_TIMESTAMP_EXCHANGE_PROPERTY, Instant.class)
                .toEpochMilli())));
    exchange.getIn().setBody(updateObj);
  }
}
