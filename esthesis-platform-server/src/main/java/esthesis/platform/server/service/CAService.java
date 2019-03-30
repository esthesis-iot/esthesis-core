package esthesis.platform.server.service;

import com.eurodyn.qlack.common.exception.QCouldNotSaveException;
import com.eurodyn.qlack.common.exception.QMutationNotPermittedException;
import com.eurodyn.qlack.fuse.crypto.CryptoCAService;
import com.eurodyn.qlack.fuse.crypto.dto.CPPPemHolderDTO;
import com.eurodyn.qlack.fuse.crypto.dto.CreateCADTO;
import com.eurodyn.qlack.fuse.crypto.dto.CreateCADTO.CreateCADTOBuilder;
import com.eurodyn.qlack.fuse.crypto.dto.CreateKeyPairDTO;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.config.AppConstants.Audit;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.CaDTO;
import esthesis.platform.server.mapper.CaMapper;
import esthesis.platform.server.model.Ca;
import esthesis.platform.server.repository.CARepository;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

@Service
@Transactional
@Validated
public class CAService extends BaseService<CaDTO, Ca> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(CAService.class.getName());

  private final CryptoCAService cryptoCAService;
  private final SecurityService securityService;
  private final AppProperties appProperties;
  private final AuditServiceProxy auditService;
  private final ObjectMapper objectMapper;
  private final CaMapper caMapper;
  private final CARepository caRepository;

  public CAService(CryptoCAService cryptoCAService,
      SecurityService securityService, AppProperties appProperties,
      AuditServiceProxy auditService, ObjectMapper objectMapper, CaMapper caMapper,
      CARepository caRepository) {
    this.cryptoCAService = cryptoCAService;
    this.securityService = securityService;
    this.appProperties = appProperties;
    this.auditService = auditService;
    this.objectMapper = objectMapper;
    this.caMapper = caMapper;
    this.caRepository = caRepository;
  }

  public Ca findEntityByCN(String caCN) {
    return caRepository.findByCn(caCN);
  }

  @Override
  public CaDTO save(CaDTO caDTO) {
    // CAs can not be edited, so throw an exception in that case.
    if (caDTO.getId() != null) {
      throw new QMutationNotPermittedException("A CA can not be edited once created.");
    }

    // Create the CA.
    try {
      final CreateCADTOBuilder createCADTOBuilder = CreateCADTO.builder()
          .createKeyPairRequestDTO(CreateKeyPairDTO.builder()
              .keySize(appProperties.getSecurityCaKeypairKeySize())
              .generatorAlgorithm(appProperties.getSecurityCaKeypairGeneratorAlgorithm())
              .generatorProvider(appProperties.getSecurityCaKeypairGeneratorProvider())
              .secretAlgorithm(appProperties.getSecurityCaKeypairSecrectAlgorithm())
              .secretProvider(appProperties.getSecurityCaKeypairSecrectProvider())
              .build())
          .subjectCN(caDTO.getCn())
          .locale(Locale.US)
          .serial(BigInteger.valueOf(1))
          .signatureProvider(appProperties.getSecurityCaKeypairSignatureProvider())
          .signatureAlgorithm(appProperties.getSecurityCaKeypairSignatureAlgorithm())
          .validFrom(Instant.now())
          .validTo(caDTO.getValidity());

      // If this CA has a parent CA (i.e. it is a sub-CA) fetch the details of the parent.
      if (StringUtils.isNotBlank(caDTO.getParentCa())) {
        Ca parentCa = findEntityByCN(caDTO.getParentCa());
        createCADTOBuilder
            .issuerCN(parentCa.getCn())
            .issuerPrivateKeyProvider(appProperties.getSecurityCaKeypairGeneratorProvider())
            .issuerPrivateKeyAlgorithm(appProperties.getSecurityCaKeypairGeneratorAlgorithm())
            .issuerPrivateKey(securityService.decrypt(parentCa.getPrivateKey()));
      }

      final CPPPemHolderDTO cppPemHolderDTO = cryptoCAService.createCA(createCADTOBuilder.build());
      caDTO.setCertificate(cppPemHolderDTO.getCertificate());
      caDTO.setPrivateKey(securityService.encrypt(cppPemHolderDTO.getPrivateKey()));
      caDTO.setPublicKey(cppPemHolderDTO.getPublicKey());
      caDTO.setIssued(Instant.now());

      caDTO = super.save(caDTO);

      // Audit.
      auditService.info(Audit.EVENT_CA, MessageFormat.format("CA {0} created.", caDTO.getCn()));

      return caDTO;
    } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidKeySpecException | IOException | OperatorCreationException e) {
      throw new QCouldNotSaveException("Could not save CA.", e);
    }
  }

  public String backup(long id) throws IOException {
    final CaDTO caDTO = caMapper.map(ReturnOptional.r(caRepository.findById(id)));
    caDTO.setPrivateKey(securityService.decrypt(caDTO.getPrivateKey()));

    return objectMapper.writeValueAsString(caDTO);
  }

  public void restore(String backup) throws IOException {
    // Create a local copy of the system's ObjectMapper in order to overwrite Access.READ_ONLY attributes of the
    // underlying object.
    ObjectMapper localObjectMapper = objectMapper.copy();
    localObjectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
    final CaDTO caDTO = localObjectMapper.readValue(backup, CaDTO.class);
    caDTO.setPrivateKey(securityService.encrypt(caDTO.getPrivateKey()));

    caRepository.save(caMapper.map(caDTO));
  }

  public List<CaDTO> getEligibleForSigning() {
    return caMapper.map(caRepository.getAllByPrivateKeyIsNotNullOrderByCn());
  }
}

