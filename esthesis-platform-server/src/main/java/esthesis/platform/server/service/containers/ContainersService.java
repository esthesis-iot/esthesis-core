package esthesis.platform.server.service.containers;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import esthesis.platform.server.config.AppConstants.Virtualization.Type;
import esthesis.platform.server.dto.ContainerDTO;
import esthesis.platform.server.dto.VirtualizationDTO;
import esthesis.platform.server.dto.WebSocketMessageDTO;
import esthesis.platform.server.service.VirtualizationService;
import esthesis.platform.server.service.WebSocketService;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Validated
@Transactional
public class ContainersService {

  private final VirtualizationService virtualizationService;
  private final DockerEngineService dockerEngineService;
  private final DockerSwarmService dockerSwarmService;
  private final WebSocketService webSocketService;

  public ContainersService(VirtualizationService virtualizationService, DockerEngineService dockerEngineService,
      DockerSwarmService dockerSwarmService, WebSocketService webSocketService) {
    this.virtualizationService = virtualizationService;
    this.dockerEngineService = dockerEngineService;
    this.dockerSwarmService = dockerSwarmService;
    this.webSocketService = webSocketService;
  }

  @Async
  public void deploy(ContainerDTO containerDTO)
  throws DockerException, InterruptedException, DockerCertificateException, NoSuchPaddingException,
         InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException,
         BadPaddingException, InvalidAlgorithmParameterException {
    // Find the type of virtualization infrastructure in which the container is to be deployed at.
    final VirtualizationDTO virtualizationDTO = virtualizationService.findById(containerDTO.getServer());

    switch (virtualizationDTO.getServerType()) {
      case Type.DOCKER_ENGINE:
        dockerEngineService.deploy(containerDTO, virtualizationDTO);
        break;
      case Type.DOCKER_SWARM:
        dockerSwarmService.deploy(containerDTO, virtualizationDTO);
        break;
    }

    webSocketService.publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload("Deployment finished.").build());
  }
}
