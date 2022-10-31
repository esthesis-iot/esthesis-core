package esthesis.services.application.impl.service;

import esthesis.service.application.dto.DTValueReply;
import esthesis.service.dataflow.resource.DataflowSystemResource;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class DTService {

  @Inject
  @RestClient
  DataflowSystemResource dataflowSystemResource;

  @PostConstruct
  void init() {
    // Find which Redis server is being used.
  }

  public DTValueReply find(String hardwareId, String category,
      String measurement) {
    System.out.println("REDIS: : " +
        dataflowSystemResource.getRedisSetup());

    return new DTValueReply()
        .setHardwareId(hardwareId)
        .setCategory(category)
        .setMeasruement(measurement)
        .setValueType("String")
        .setValue("Hello World");
  }
}
