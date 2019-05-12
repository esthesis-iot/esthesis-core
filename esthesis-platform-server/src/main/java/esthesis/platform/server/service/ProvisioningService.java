package esthesis.platform.server.service;

import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.dto.ProvisioningDTO;
import esthesis.platform.server.mapper.ProvisioningMapper;
import esthesis.platform.server.model.Provisioning;
import esthesis.platform.server.repository.ProvisioningRepository;
import esthesis.platform.server.repository.TagRepository;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Transactional
@Validated
public class ProvisioningService extends BaseService<ProvisioningDTO, Provisioning> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(ProvisioningService.class.getName());

  private final ProvisioningMapper provisioningMapper;
  private final ProvisioningRepository provisioningRepository;
  private final TagRepository tagRepository;
  private final SecurityService securityService;

  public ProvisioningService(ProvisioningMapper provisioningMapper,
    ProvisioningRepository provisioningRepository,
    TagRepository tagRepository, SecurityService securityService) {
    this.provisioningMapper = provisioningMapper;
    this.provisioningRepository = provisioningRepository;
    this.tagRepository = tagRepository;
    this.securityService = securityService;
  }

  @Override
  public Page<ProvisioningDTO> findAll(Predicate predicate, Pageable pageable) {
    return provisioningMapper.map(provisioningRepository.findAll(predicate, pageable));
  }

  public ProvisioningDTO save(ProvisioningDTO provisioningDTO) {
    // Do not use the generic save from BaseService as we do not want to map the binary content
    // of the provisioning package unnecessarily.
    if (provisioningDTO.getId() != null) {
      final Provisioning provisioning = ReturnOptional.r(provisioningRepository
        .findById(provisioningDTO.getId()));
      provisioning.setDefaultIP(provisioningDTO.isDefaultIP());
      provisioning.setDescription(provisioningDTO.getDescription());
      provisioning.setName(provisioningDTO.getName());
      provisioning.setPackageVersion(provisioningDTO.getPackageVersion());
      provisioning.setState(provisioningDTO.isState());
      provisioning
        .setTags(
          IteratorUtils.toList(tagRepository.findAllById(provisioningDTO.getTags()).iterator()));
      provisioningRepository.save(provisioning);
      return provisioningMapper.map(provisioning);
    } else {
      try {
        StopWatch stopwatch = StopWatch.createStarted();
        provisioningDTO.setSignature(
          Base64.encodeBase64String(securityService.sign(provisioningDTO.getFileContent())));
        LOGGER.log(Level.FINE, "Signing took {0} msec.", stopwatch.getTime());
      } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException |
        BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException |
        InvalidKeySpecException | SignatureException e) {
        LOGGER.log(Level.SEVERE, "Could not sign provisioning pacakge");
      }
      return provisioningMapper
        .map(provisioningRepository.save(provisioningMapper.map(provisioningDTO)));
    }
  }
}
