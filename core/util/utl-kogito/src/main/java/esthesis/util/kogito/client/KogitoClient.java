package esthesis.util.kogito.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.util.kogito.dto.InstanceDTO;
import esthesis.util.kogito.dto.TaskDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class KogitoClient {

  @ConfigProperty(name = "esthesis.kogito.client.url")
  String baseUrl;

  @Inject
  ObjectMapper mapper;

  private ExecutorService executorService = Executors.newCachedThreadPool();
  private Client client;

  @Inject
  public KogitoClient() {
    client = ClientBuilder.newBuilder()
        .executorService(executorService)
        .build();
  }

  private InstanceDTO nodeToInstance(JsonNode node) {
    InstanceDTO instanceDTO = new InstanceDTO();
    instanceDTO.setId(node.get("id").asText());
    node.fields().forEachRemaining(entry -> {
      if (!entry.getKey().equals("id")) {
        instanceDTO.getData().put(entry.getKey(), entry.getValue().asText());
      }
    });

    return instanceDTO;
  }

  public List<InstanceDTO> getInstances(String processId) throws JsonProcessingException {
    log.debug("Getting instances for process id '{}'.", processId);
    List<InstanceDTO> instances = new ArrayList<>();

    JsonNode json = mapper.readTree(client
        .target(baseUrl)
        .path(processId)
        .request(MediaType.APPLICATION_JSON)
        .get(String.class));

    if (json.isArray()) {
      for (JsonNode node : json) {
        instances.add(nodeToInstance(node));
      }
    }

    return instances;
  }

  public InstanceDTO startInstance(String processId, Object body) throws JsonProcessingException {
    log.debug("Starting instance for processId '{}'.", processId);
    JsonNode json = mapper.readTree(client
        .target(baseUrl)
        .path(processId)
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.json(body), String.class));

    return nodeToInstance(json);
  }

  public InstanceDTO deleteInstance(String processId, String instanceId)
  throws JsonProcessingException {
    log.debug("Deleting process instance '{}'.", instanceId);
    JsonNode json = mapper.readTree(client
        .target(baseUrl)
        .path(processId)
        .path(instanceId)
        .request(MediaType.APPLICATION_JSON)
        .delete(String.class));

    return nodeToInstance(json);
  }

  public List<TaskDTO> getTasks(String processId, String instanceId) {
    log.debug("Finding tasks for process id '{}', instance id '{}'", processId, instanceId);
    return List.of(client
        .target(baseUrl)
        .path(processId)
        .path(instanceId)
        .path("tasks")
        .request(MediaType.APPLICATION_JSON)
        .get(TaskDTO[].class));
  }

  public InstanceDTO completeTask(String processId, String instanceId, String taskName,
      String taskId) throws JsonProcessingException {
    log.debug("Completing task '{}' for process instance '{}'.", taskId, instanceId);
    JsonNode json = mapper.readTree(client
        .target(baseUrl)
        .path(processId)
        .path(instanceId)
        .path(taskName)
        .path(taskId)
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.json("{}"), String.class));

    return nodeToInstance(json);
  }

}
