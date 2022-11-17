package esthesis.dataflows.pingupdater.service;

import com.mongodb.client.model.Filters;
import esthesis.avro.EsthesisDataMessage;
import esthesis.common.exception.QMismatchException;
import esthesis.dataflow.common.DflUtils;
import java.time.Instant;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

@Slf4j
@ApplicationScoped
public class PingService {

  // The name of the field in the message data that contains the ping value.
  private final static String PING_MEASUREMENT = "ping";

  // The MongoDB field name for the ping value.
  private final static String PING_ATTRIBUTE_NAME = "lastSeen";

  public void searchForExistingDevice(Exchange exchange) {
    // Get the message from the exchange.
    EsthesisDataMessage esthesisMessage = exchange.getIn()
        .getBody(EsthesisDataMessage.class);

    // Create search criteria for the MongoDB query.
    Bson equalsClause = Filters.eq("hardwareId",
        esthesisMessage.getHardwareId());

    // Set search criteria as a header in the exchange.
    exchange.getIn().setHeader(MongoDbConstants.CRITERIA, equalsClause);
  }

  public void updateTimestamp(Exchange exchange) {
    log.debug("Updating ping timestamp for hardware ID '{}'.",
        exchange.getIn().getHeader(KafkaConstants.KEY));
    // Get the message from the exchange.
    EsthesisDataMessage esthesisMessage = exchange.getIn()
        .getBody(EsthesisDataMessage.class);

    // Update.
    esthesisMessage.getPayload().getValues().stream().filter(
            value -> value.getName().equals(PING_MEASUREMENT)).findFirst()
        .ifPresentOrElse(
            value -> {
              BsonDocument updateObj = new BsonDocument().append("$set",
                  new BsonDocument(PING_ATTRIBUTE_NAME,
                      new BsonDateTime(
                          Instant.parse(value.getValue()).toEpochMilli())));
              exchange.getIn().setBody(updateObj);
              log.debug("Ping timestamp updated to '{}'.", value.getValue());
            },
            () -> {
              throw new QMismatchException(
                  "No ping timestamp found in payload '{}'.",
                  StringUtils.abbreviate(
                      esthesisMessage.getPayload().toString(),
                      DflUtils.MESSAGE_LOG_ABBREVIATION_LENGTH));
            });
  }
}
