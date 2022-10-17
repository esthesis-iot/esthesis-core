package esthesis.dataflows.pingupdater.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.camel.Exchange;

@ApplicationScoped
public class PingService {

  @Inject
  ObjectMapper objectMapper;

  public void updateTimestamp(Exchange exchange) {
//    BsonDocument updateObj = new BsonDocument().append("$set",
//        new BsonDocument(PING_ATTRIBUTE_NAME,
//            new BsonDateTime(exchange.getProperty(
//                    PING_TIMESTAMP_EXCHANGE_PROPERTY, Instant.class)
//                .toEpochMilli())));
//    exchange.getIn().setBody(updateObj);
  }
}
