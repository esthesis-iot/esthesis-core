package esthesis.platform.server.service.containers;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.mount.Mount;
import com.spotify.docker.client.messages.swarm.ContainerSpec;
import com.spotify.docker.client.messages.swarm.EndpointSpec;
import com.spotify.docker.client.messages.swarm.NetworkAttachmentConfig;
import com.spotify.docker.client.messages.swarm.PortConfig;
import com.spotify.docker.client.messages.swarm.ServiceMode;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.TaskSpec;
import esthesis.platform.server.config.AppConstants.Virtualization.Container.RestartPolicy;
import esthesis.platform.server.dto.ContainerDTO;
import esthesis.platform.server.dto.VirtualizationDTO;
import esthesis.platform.server.dto.WebSocketMessageDTO;
import esthesis.platform.server.service.WebSocketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class DockerSwarmService {

  private final DockerUtilService dockerUtilService;
  private final WebSocketService webSocketService;
  public static final String NETWORK_DRIVER_OVERLAY = "overlay";

  public DockerSwarmService(DockerUtilService dockerUtilService,
      WebSocketService webSocketService) {
    this.dockerUtilService = dockerUtilService;
    this.webSocketService = webSocketService;
  }

  private List<Mount> setupVolumes(ContainerDTO containerDTO) {webSocketService.publish(
      WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload("Preparing volumes.").build());

    return containerDTO.getVolumes().stream()
        .map(o -> Mount.builder().source(o.getSource()).target(o.getTarget()).build())
        .collect(Collectors.toList());
  }

  private com.spotify.docker.client.messages.swarm.RestartPolicy setupRestartPolicy(ContainerDTO containerDTO) {
    webSocketService.publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload("Preparing restart policy.").build());
    switch (containerDTO.getRestart()) {
      case RestartPolicy.ON_FAILURE:
        return com.spotify.docker.client.messages.swarm.RestartPolicy.builder().condition(
            com.spotify.docker.client.messages.swarm.RestartPolicy.RESTART_POLICY_ON_FAILURE).build();
      case RestartPolicy.ANY:
        return com.spotify.docker.client.messages.swarm.RestartPolicy.builder().condition(
            com.spotify.docker.client.messages.swarm.RestartPolicy.RESTART_POLICY_ANY).build();
      case RestartPolicy.NONE:
      default:
        return com.spotify.docker.client.messages.swarm.RestartPolicy.builder().condition(
            com.spotify.docker.client.messages.swarm.RestartPolicy.RESTART_POLICY_NONE).build();
    }
  }

  private EndpointSpec setupPortBindings(ContainerDTO containerDTO) {
    webSocketService.publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload("Preparing port bindings.").build());
    return EndpointSpec.builder().ports(
        containerDTO.getPorts().stream().map(o ->
            PortConfig.builder().publishedPort(o.getHost()).targetPort(o.getContainer()).protocol(o.getProtocol())
                .build())
            .collect(Collectors.toList())
    ).build();
  }

  private NetworkAttachmentConfig setupNetwoking(ContainerDTO containerDTO, DockerClient dockerClient)
      throws DockerException, InterruptedException {
    // Create network if not exists.
    String networkId = dockerUtilService.createNetwork(containerDTO, dockerClient, NETWORK_DRIVER_OVERLAY);

    // Prepare network configuration.
    return NetworkAttachmentConfig.builder().target(networkId).build();
  }

  public void deploy(ContainerDTO containerDTO, VirtualizationDTO virtualizationDTO)
      throws DockerCertificateException, DockerException, InterruptedException {
    // Setup docker client.
    final DockerClient dockerClient = DefaultDockerClient.builder()
        .uri(virtualizationDTO.getIpAddress())
        .registryAuthSupplier(dockerUtilService.setupRegistryAuth(containerDTO))
        .dockerCertificates(dockerUtilService.setupClientCertificate(containerDTO, virtualizationDTO))
        .build();

    // Setup host for the container.
    final ContainerSpec containerSpec = ContainerSpec.builder()
        .image(containerDTO.getImage())
        .env(dockerUtilService.setupEnvParams(containerDTO))
        .mounts(setupVolumes(containerDTO))
        .build();

    // Setup Swarm service specs.
    final ServiceSpec.Builder serviceSpecBuilder = ServiceSpec.builder()
        .taskTemplate(TaskSpec.builder()
            .containerSpec(containerSpec)
            .restartPolicy(setupRestartPolicy(containerDTO))
            .build())
        .mode(ServiceMode.withReplicas(containerDTO.getScale()))
        .name(containerDTO.getName())
        .networks(setupNetwoking(containerDTO, dockerClient))
        .endpointSpec(setupPortBindings(containerDTO));

    // Create swarm service.
    webSocketService.publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload("Creating service.").build());
    dockerClient.createService(serviceSpecBuilder.build());
  }
}
