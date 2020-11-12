package esthesis.platform.server.service;

import com.eurodyn.qlack.common.exception.QCouldNotSaveException;
import com.eurodyn.qlack.common.exception.QMutationNotPermittedException;
import com.eurodyn.qlack.fuse.crypto.dto.CPPPemHolderDTO;
import com.eurodyn.qlack.fuse.crypto.dto.CreateCADTO;
import com.eurodyn.qlack.fuse.crypto.dto.CreateCADTO.CreateCADTOBuilder;
import com.eurodyn.qlack.fuse.crypto.dto.CreateKeyPairDTO;
import com.eurodyn.qlack.fuse.crypto.service.CryptoCAService;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.CaDTO;
import esthesis.platform.server.mapper.CaMapper;
import esthesis.platform.server.model.Ca;
import esthesis.platform.server.repository.CARepository;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@Validated
@Transactional
public class CAService extends BaseService<CaDTO, Ca> {

  private final CryptoCAService cryptoCAService;
  private final SecurityService securityService;
  private final AppProperties appProperties;
  private final ObjectMapper objectMapper;
  private final CaMapper caMapper;
  private final CARepository caRepository;

  public CAService(CryptoCAService cryptoCAService,
    SecurityService securityService, AppProperties appProperties,
    ObjectMapper objectMapper, CaMapper caMapper,
    CARepository caRepository) {
    this.cryptoCAService = cryptoCAService;
    this.securityService = securityService;
    this.appProperties = appProperties;
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
          .keySize(appProperties.getSecurityAsymmetricKeySize())
          .keyPairGeneratorAlgorithm(appProperties.getSecurityAsymmetricKeyAlgorithm())
          .build())
        .subjectCN(caDTO.getCn())
        .locale(Locale.US)
        .serial(BigInteger.valueOf(1))
        .signatureAlgorithm(appProperties.getSecurityAsymmetricSignatureAlgorithm())
        .validFrom(Instant.now())
        .validTo(caDTO.getValidity());

      // If this CA has a parent CA (i.e. it is a sub-CA) fetch the details of the parent.
      if (StringUtils.isNotBlank(caDTO.getParentCa())) {
        Ca parentCa = findEntityByCN(caDTO.getParentCa());
        createCADTOBuilder
          .issuerCN(parentCa.getCn())
          .issuerPrivateKeyAlgorithm(appProperties.getSecurityAsymmetricKeyAlgorithm())
          .issuerPrivateKey(new String(securityService.decrypt(parentCa.getPrivateKey()),
            StandardCharsets.UTF_8));
      }

      final CPPPemHolderDTO cppPemHolderDTO = cryptoCAService.createCA(createCADTOBuilder.build());
      caDTO.setCertificate(cppPemHolderDTO.getCertificate());
      caDTO.setPrivateKey(securityService.encrypt(cppPemHolderDTO.getPrivateKey()));
      caDTO.setPublicKey(cppPemHolderDTO.getPublicKey());
      caDTO.setIssued(Instant.now());

      caDTO = super.save(caDTO);

      return caDTO;
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | OperatorCreationException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchProviderException e) {
      throw new QCouldNotSaveException("Could not save CA.", e);
    }
  }

  public String backup(long id)
  throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
         InvalidAlgorithmParameterException {
    final CaDTO caDTO = caMapper.map(ReturnOptional.r(caRepository.findById(id)));
    caDTO.setPrivateKey(new String(securityService.decrypt(caDTO.getPrivateKey()),
      StandardCharsets.UTF_8));

    return objectMapper.writeValueAsString(caDTO);
  }

  public void restore(String backup)
  throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
         InvalidAlgorithmParameterException {
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

