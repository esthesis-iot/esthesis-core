package esthesis.platform.server.resource;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import esthesis.platform.server.dto.ContainerDTO;
import esthesis.platform.server.service.containers.ContainersService;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Validated
@RestController
@RequestMapping("/containers")
public class ContainersResource {

  private final ContainersService containersService;

  public ContainersResource(ContainersService containersService) {
    this.containersService = containersService;
  }

  /**
   * Deploy the container. We do not wrap this REST endpoint as we want to present any error messages back to the
   * front-end.
   */
  @PostMapping
  public ResponseEntity deploy(@Valid @RequestBody ContainerDTO containerDTO)
  throws DockerCertificateException, DockerException, InterruptedException, NoSuchPaddingException,
         InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException,
         BadPaddingException, InvalidKeyException {
    containersService.deploy(containerDTO);

    return ResponseEntity.ok().build();
  }
}
