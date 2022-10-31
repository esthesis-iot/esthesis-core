package esthesis.service.dataflow.impl.resource;

import esthesis.service.dataflow.dto.Dataflow;
import esthesis.service.dataflow.dto.MatchedMqttServer;
import esthesis.service.dataflow.impl.service.DataflowService;
import esthesis.service.dataflow.resource.DataflowSystemResource;
import java.util.List;
import javax.inject.Inject;

public class DataflowSystemResourceImpl implements DataflowSystemResource {

  @Inject
  DataflowService dataflowService;

  @Override
  public MatchedMqttServer matchMqttServerByTags(List<String> tags) {
    return dataflowService.matchMqttServerByTags(tags);
  }

  @Override
  public List<Dataflow> getRedisSetup() {
    return dataflowService.getRedisSetup();
  }

}
