package esthesis.service.dataflow.impl.resource;

import esthesis.service.dataflow.dto.MatchedMqttServerDTO;
import esthesis.service.dataflow.impl.service.DataflowService;
import esthesis.service.dataflow.resource.DataflowSystemResource;
import java.util.List;
import javax.inject.Inject;

public class DataflowSystemResourceImpl implements DataflowSystemResource {

  @Inject
  DataflowService dataflowService;

  @Override
  public MatchedMqttServerDTO matchMqttServerByTags(List<String> tags) {
    return dataflowService.matchMqttServerByTags(tags);
  }

}
