package esthesis.platform.server.service.containers;

import com.spotify.docker.client.DockerCertificatesStore;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListNetworksParam;
import com.spotify.docker.client.auth.FixedRegistryAuthSupplier;
import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkConfig.Builder;
import com.spotify.docker.client.messages.RegistryAuth;
import esthesis.platform.server.config.AppConstants.Virtualization.Security;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.docker.DockerInMemoryCertificates;
import esthesis.platform.server.dto.CertificateDTO;
import esthesis.platform.server.dto.ContainerDTO;
import esthesis.platform.server.dto.VirtualizationDTO;
import esthesis.platform.server.dto.WebSocketMessageDTO;
import esthesis.platform.server.model.Ca;
import esthesis.platform.server.service.CAService;
import esthesis.platform.server.service.CertificatesService;
import esthesis.platform.server.service.SecurityService;
import esthesis.platform.server.service.WebSocketService;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@Validated
@Transactional
public class DockerUtilService {

  private final WebSocketService webSocketService;
  private final CertificatesService certificatesService;
  private final CAService caService;
  private final SecurityService securityService;
  private final AppProperties appProperties;

  public DockerUtilService(WebSocketService webSocketService,
    CertificatesService certificatesService, CAService caService,
    SecurityService securityService, AppProperties appProperties) {
    this.webSocketService = webSocketService;
    this.certificatesService = certificatesService;
    this.caService = caService;
    this.securityService = securityService;
    this.appProperties = appProperties;
  }

  public DockerCertificatesStore setupClientCertificate(ContainerDTO containerDTO,
    VirtualizationDTO virtualizationDTO)
  throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
         InvalidAlgorithmParameterException, DockerCertificateException, IOException {
    DockerCertificatesStore dockerCertificatesStore = null;

    if (virtualizationDTO.getSecurity() == Security.CERTIFICATE) {
      webSocketService.publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId())
        .payload("Preparing client certificates.").build());
      final CertificateDTO clientCertificate = certificatesService
        .findById(virtualizationDTO.getCertificate());

      // Find the certificate of the CA of the client certificate.
      final Ca ca = caService.findEntityByCN(clientCertificate.getIssuer());

      dockerCertificatesStore = DockerInMemoryCertificates.builder()
        .caCert(ca.getCertificate())
        .clientCert(clientCertificate.getCertificate())
        .clientKey(new String(securityService.decrypt(clientCertificate.getPrivateKey()),
          StandardCharsets.UTF_8))
        .securityAlgorithm(appProperties.getSecurityAsymmetricKeyAlgorithm())
        .build().get();
    }

    return dockerCertificatesStore;
  }

  public RegistryAuthSupplier setupRegistryAuth(ContainerDTO containerDTO) {
    final RegistryAuth auth;
    webSocketService.publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId())
      .payload("Preparing registry authentication.").build());
    if (StringUtils.isNotBlank(containerDTO.getRegistryUsername()) && StringUtils
      .isNotBlank(containerDTO.getRegistryPassword())) {
      auth = RegistryAuth.builder()
        .username(containerDTO.getRegistryUsername())
        .password(containerDTO.getRegistryPassword())
        .build();
    } else {
      auth = RegistryAuth.builder().build();
    }

    return new FixedRegistryAuthSupplier(auth, null);
  }

  public List<String> setupEnvParams(ContainerDTO containerDTO) {
    webSocketService.publish(WebSocketMessageDTO.builder().topic(containerDTO.getWsId())
      .payload("Preparing environmental parameters.").build());
    // Add environmental parameters.
    List<String> environment = new ArrayList<>();
    containerDTO.getEnv().stream().forEach(o -> {
      environment.add(o.getEnvName() + "=" + o.getEnvValue());
    });

    return environment;
  }

  public String createNetwork(ContainerDTO containerDTO, DockerClient dockerClient)
  throws DockerException, InterruptedException {
    return createNetwork(containerDTO, dockerClient, null);
  }

  public String createNetwork(ContainerDTO containerDTO, DockerClient dockerClient, String driver)
  throws DockerException, InterruptedException {
    String networkId = null;

    webSocketService.publish(
      WebSocketMessageDTO.builder().topic(containerDTO.getWsId()).payload("Preparing networking.")
        .build());
    if (StringUtils.isNotBlank(containerDTO.getNetwork())) {
      final List<Network> networks = dockerClient
        .listNetworks(ListNetworksParam.byNetworkName(containerDTO.getNetwork()));

      if (networks.size() == 0) {
        webSocketService.publish(
          WebSocketMessageDTO.builder().topic(containerDTO.getWsId())
            .payload("Creating network " + containerDTO.getNetwork() + ".").build());
        // Prepare network configuration.
        final Builder networkConfigBuilder = NetworkConfig.builder()
          .checkDuplicate(true)
          .attachable(true)
          .name(containerDTO.getNetwork());

        // Setup network driver if requested.
        if (StringUtils.isNotBlank(driver)) {
          networkConfigBuilder.driver(driver);
        }

        // Create the network and obtain its id.
        networkId = dockerClient.createNetwork(networkConfigBuilder.build()).id();
      } else {
        // Since the network already existed return its id.
        networkId = networks.get(0).id();
      }
    }

    return networkId;
  }
}

