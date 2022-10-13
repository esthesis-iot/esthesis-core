package esthesis.dataflows.pingupdater.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.dataflow.common.messages.EsthesisMessage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.camel.Exchange;

@ApplicationScoped
public class PingService {

  @Inject
  ObjectMapper objectMapper;

  public void process(Exchange exchange) {
    System.out.println("GOT MESSAGE!");
    // Dump headers.
    exchange.getIn().getHeaders().forEach((s, o) -> {
      System.out.println(s + ": " + o);
    });

    System.out.println(
        exchange.getIn().getBody()
    );
  }

  public void extractMessageTimestamp(Exchange exchange)
  throws JsonProcessingException {
    System.out.println(">>>>>>>>>>>>>>>>>>> ");
    System.out.println(exchange.getIn().getBody());
    EsthesisMessage esthesisMessage = objectMapper.readValue(
        exchange.getIn().getBody(String.class), EsthesisMessage.class);
    System.out.println(esthesisMessage);
//    Instant timestamp = Instant.parse(esthesisMessage.getPayload());
//    exchange.setProperty("EsthesisPingTimestamp", timestamp);
  }
}
