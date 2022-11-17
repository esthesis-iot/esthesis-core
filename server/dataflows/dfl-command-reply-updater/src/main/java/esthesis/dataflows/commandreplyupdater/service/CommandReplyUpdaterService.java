package esthesis.dataflows.commandreplyupdater.service;

import esthesis.avro.EsthesisCommandReplyMessage;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class CommandReplyUpdaterService {

  @ConfigProperty(name = "quarkus.application.name")
  String appName;

  public void createMongoEntity(Exchange exchange) {
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    EsthesisCommandReplyMessage msg = exchange.getIn()
        .getBody(EsthesisCommandReplyMessage.class);

    System.out.println(msg);

  }
}
