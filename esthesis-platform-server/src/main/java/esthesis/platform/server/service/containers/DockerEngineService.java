package esthesis.platform.server.service.containers;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import esthesis.platform.server.config.AppConstants.Virtualization.Container;
import esthesis.platform.server.config.AppConstants.Virtualization.Container.RestartPolicy;
import esthesis.platform.server.dto.ContainerDTO;
import esthesis.platform.server.dto.VirtualizationDTO;
import esthesis.platform.server.dto.WebSocketMessageDTO;
import esthesis.platform.server.service.WebSocketService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class DockerEngineService {

  private final DockerUtilService dockerUtilService;
  private final WebSocketService webSocketService;

  public DockerEngineService(DockerUtilService dockerUtilService,
      WebSocketService webSocketService) {
    this.dockerUtilService = dockerUtilService;
    this.webSocketService = webSocketService;
  }

  private List<String> setupVolumes(ContainerDTO containerDTO) {
    webSocketService
        .publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload("Preparing volumes.").build());
    return containerDTO.getVolumes().stream().map(o -> o.getSource() + ":" + o.getTarget())
        .collect(Collectors.toList());
  }

  private HostConfig.RestartPolicy setupRestartPolicy(ContainerDTO containerDTO) {
    webSocketService.publish(
        WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload("Preparing restart policy.").build());
    switch (containerDTO.getRestart()) {
      case RestartPolicy.ALWAYS:
        return HostConfig.RestartPolicy.always();
      case RestartPolicy.UNLESS_STOPPED:
        return HostConfig.RestartPolicy.unlessStopped();
      case Container.RestartPolicy.ON_FAILURE:
        return HostConfig.RestartPolicy.onFailure(Integer.MAX_VALUE);
      case Container.RestartPolicy.NONE:
      default:
        return null;
    }
  }

  private void setupNetworking(ContainerDTO containerDTO, DockerClient dockerClient, String containerId)
      throws DockerException, InterruptedException {
    String networkId = dockerUtilService.createNetwork(containerDTO, dockerClient);
    if (networkId != null) {
      dockerClient.connectToNetwork(containerId, networkId);
    }
  }

  private Map<String, List<PortBinding>> setupPortBindings(ContainerDTO containerDTO) {
    webSocketService.publish(
        WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload("Preparing port bindings.").build());
    final Map<String, List<PortBinding>> portBindings = new HashMap();
    containerDTO.getPorts().stream().forEach(o -> {
      portBindings.put(o.getContainer() + "/" + o.getProtocol().toLowerCase(),
          Arrays.asList(PortBinding.of("", o.getHost())));
    });

    return portBindings;
  }

  private void pullImage(ContainerDTO containerDTO, DockerClient dockerClient)
      throws DockerException, InterruptedException {
    if (!containerDTO.getImage().contains(":")) {
      containerDTO.setImage(containerDTO.getImage() + ":latest");
    }

    if (!dockerClient.listImages(ListImagesParam.byName(containerDTO.getImage())).stream()
        .filter(o -> o.repoTags().contains(containerDTO.getImage())).findFirst().isPresent()) {
      webSocketService.publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId())
          .payload("Pulling image " + containerDTO.getImage() + ".").build());
      dockerClient.pull(containerDTO.getImage(), message -> {
        if (message.progressDetail() != null && message.progressDetail().current() != null && message.progressDetail()
            .total() != null) {
          webSocketService.publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload(".").build());
        }
      });
    }
  }

  private Set<String> setupExposedPorts(ContainerDTO containerDTO) {
    return containerDTO.getPorts().stream()
        .map(containerPortDTO -> containerPortDTO.getContainer() + "/" + containerPortDTO.getProtocol().toLowerCase())
        .collect(Collectors.toSet());
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
    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(containerDTO.getImage())
        .env(dockerUtilService.setupEnvParams(containerDTO))
        .portSpecs()
        .hostConfig(HostConfig.builder()
            .portBindings(setupPortBindings(containerDTO))
            .appendBinds(setupVolumes(containerDTO))
            .restartPolicy(setupRestartPolicy(containerDTO))
            .build())
        .exposedPorts(setupExposedPorts(containerDTO))
        .build();

    // Check if image already exists, otherwise pull it.
    pullImage(containerDTO, dockerClient);

    // Create container.
    webSocketService
        .publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload("Creating container.").build());
    final ContainerCreation container;
    if (StringUtils.isNotBlank(containerDTO.getName())) {
      container = dockerClient.createContainer(containerConfig, containerDTO.getName());
    } else {
      container = dockerClient.createContainer(containerConfig);
    }

    // Handle networking.
    setupNetworking(containerDTO, dockerClient, container.id());

    // Start container.
    webSocketService
        .publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload("Starting container.").build());
    dockerClient.startContainer(container.id());
  }
}
