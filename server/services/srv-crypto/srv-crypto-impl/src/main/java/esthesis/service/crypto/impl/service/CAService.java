package esthesis.service.crypto.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.exception.QCouldNotSaveException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QMutationNotPermittedException;
import esthesis.service.common.BaseService;
import esthesis.service.common.validation.CVException;
import esthesis.service.crypto.dto.CAHolderDTO;
import esthesis.service.crypto.dto.CreateCARequestDTO;
import esthesis.service.crypto.dto.CreateKeyPairRequestDTO;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.form.ImportCaForm;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.panache.common.Sort;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Certificate Authority management.
 */
@Slf4j
@ApplicationScoped
public class CAService extends BaseService<CaEntity> {

  @Inject
  @RestClient
  SettingsResource settingsResource;

  @Inject
  CryptoService cryptoService;

  @Inject
  ObjectMapper mapper;

  @Override
  public CaEntity save(CaEntity caEntity) {
    // CAs can not be edited, so throw an exception in that case.
    if (caEntity.getId() != null) {
      throw new QMutationNotPermittedException("A CA can not be edited once created.");
    }

    // Check a CA with the same name doesn't already exist.
    if (findFirstByColumn("cn", caEntity.getCn()) != null) {
      new CVException<CaEntity>().addViolation("cn",
          "A Certificate Authority with CN '{}' already exists.", caEntity.getCn()).throwCVE();
    }

    // Create the CA.
    try {
      String asymmetricKeyAlgorithm =
          settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString();
      CreateCARequestDTO.CreateCARequestDTOBuilder createCARequestBuilder = CreateCARequestDTO.builder()
          .createKeyPairRequestDTO(CreateKeyPairRequestDTO.builder().keySize(
                  settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE).asInt())
              .keyPairGeneratorAlgorithm(asymmetricKeyAlgorithm).build())
          .subjectCN(caEntity.getCn()).locale(Locale.US)
          .serial(BigInteger.valueOf(1)).signatureAlgorithm(
              settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM)
                  .asString()).validFrom(Instant.now()).validTo(caEntity.getValidity());

      // If this CA has a parent CA (i.e. it is a sub-CA) fetch the details of the parent.
      if (caEntity.getParentCaId() != null) {
        CaEntity parentCaEntity = findById(caEntity.getParentCaId());
        createCARequestBuilder.issuerCN(parentCaEntity.getCn())
            .issuerPrivateKeyAlgorithm(asymmetricKeyAlgorithm)
            .issuerPrivateKey(
                cryptoService.pemToPrivateKey(parentCaEntity.getPrivateKey(),
                    asymmetricKeyAlgorithm));
        caEntity.setParentCa(parentCaEntity.getCn());
      }

      final CAHolderDTO caHolderDTO = cryptoService.createCA(createCARequestBuilder.build());
      caEntity.setCertificate(cryptoService.certificateToPEM(caHolderDTO.getCertificate()));
      caEntity.setPrivateKey(cryptoService.privateKeyToPEM(caHolderDTO.getPrivateKey()));
      caEntity.setPublicKey(cryptoService.publicKeyToPEM(caHolderDTO.getPublicKey()));
      caEntity.setIssued(Instant.now());

      return super.save(caEntity);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException |
             OperatorCreationException | NoSuchProviderException e) {
      throw new QCouldNotSaveException("Could not save CA.", e);
    }
  }

  public List<CaEntity> getEligibleForSigning() {
    return getRepository().find("privateKey != null", Sort.ascending("cn")).list();
  }

  public CaEntity importCa(ImportCaForm importCaForm) {
    try {
      CaEntity caEntity = mapper.readValue(importCaForm.getBackup().uploadedFile().toFile(),
          CaEntity.class);
      if (findById(caEntity.getId()) != null || findFirstByColumn("cn", caEntity.getCn()) != null) {
        throw new QAlreadyExistsException("A CA with this CN or id already exists.");
      }

      super.getRepository().persistOrUpdate(caEntity);
      return caEntity;
    } catch (IOException e) {
      throw new QMismatchException("Could not import CA.", e);
    }
  }
}
