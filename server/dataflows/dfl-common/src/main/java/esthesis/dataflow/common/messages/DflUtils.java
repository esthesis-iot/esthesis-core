package esthesis.dataflow.common.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.camel.Exchange;

@ApplicationScoped
public class DflUtils {

  public static final String CAMEL_HEADER_KAFKA_KEY = "Kafka.KEY";
  public static final String ESTHESIS_CAMEL_PROP_HARDWARE_ID = "esthesis"
      + ".hardwareId";

  @Inject
  private ObjectMapper objectMapper;

  public void extractHardwareIdFromKafka(Exchange exchange) {
    exchange.setProperty(ESTHESIS_CAMEL_PROP_HARDWARE_ID,
        exchange.getIn().getHeader(CAMEL_HEADER_KAFKA_KEY));
  }

  /**
   * Parses the body of an exchange and creates an EsthesisMessage from it.
   *
   * @param exchange The exchange to parse.
   */
  public EsthesisMessage parseEsthesisMessage(Exchange exchange)
  throws JsonProcessingException {
    return objectMapper.readValue(exchange.getIn().getBody(String.class),
        EsthesisMessage.class);
  }
}
