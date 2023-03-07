package esthesis.service.crypto.impl.service;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.exception.QCouldNotSaveException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QMutationNotPermittedException;
import esthesis.service.common.BaseService;
import esthesis.service.common.validation.CVExceptionContainer;
import esthesis.service.crypto.dto.CAHolderDTO;
import esthesis.service.crypto.dto.CreateCARequestDTO;
import esthesis.service.crypto.dto.CreateKeyPairRequestDTO;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.form.ImportCaForm;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.panache.common.Sort;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.ArrayList;
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

  @Override
  public CaEntity save(CaEntity caEntity) {
    // CAs can not be edited, so throw an exception in that case.
    if (caEntity.getId() != null) {
      throw new QMutationNotPermittedException("A CA can not be edited once created.");
    }

    // Check a CA with the same name doesn't already exist.
    if (findFirstByColumn("cn", caEntity.getCn()) != null) {
      new CVExceptionContainer<CaEntity>().addViolation("cn",
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
        CaEntity parentCaEntity = findById(caEntity.getParentCaId().toHexString());
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
    CaEntity caEntity = new CaEntity();
    try {
      // Set the keys into the certificate entity.
      caEntity.setCertificate(
          Files.readString(importCaForm.getCertificate().uploadedFile().toAbsolutePath()));
      caEntity.setPrivateKey(
          Files.readString(importCaForm.getPrivateKey().uploadedFile().toAbsolutePath()));
      caEntity.setPublicKey(
          Files.readString(importCaForm.getPublicKey().uploadedFile().toAbsolutePath()));

      // Extract additional certificate information.
      X509Certificate x509Certificate = cryptoService.pemToCertificate(caEntity.getCertificate());
      caEntity.setName(importCaForm.getName());
      caEntity.setIssued(x509Certificate.getNotBefore().toInstant());
      caEntity.setValidity(x509Certificate.getNotAfter().toInstant());

      //Set CN and parent CA.
      String cn = cryptoService.cleanUpCn(x509Certificate.getSubjectX500Principal().getName());
      String issuer = cryptoService.cleanUpCn(x509Certificate.getIssuerX500Principal().getName());
      caEntity.setCn(cn);
      if (!cn.equalsIgnoreCase(issuer)) {
        caEntity.setParentCa(issuer);
      }

      super.getRepository().persistOrUpdate(caEntity);
      return caEntity;
    } catch (IOException | CertificateException e) {
      throw new QMismatchException("Could not import certificate.", e);
    }
  }

  public String getPrivateKey(String caId) {
    CaEntity caEntity = findById(caId);

    return caEntity.getPrivateKey();
  }

  public String getPublicKey(String caId) {
    CaEntity caEntity = findById(caId);

    return caEntity.getPublicKey();
  }

  private List<String> getCertificate(String caId, List<String> certificteChain) {
    CaEntity caEntity = findById(caId);
    certificteChain.add(caEntity.getCertificate());

    if (caEntity.getParentCaId() != null) {
      getCertificate(caEntity.getParentCaId().toHexString(), certificteChain);
    }

    return certificteChain;
  }

  public List<String> getCertificate(String caId) {
    List<String> certificateChain = new ArrayList<>();
    getCertificate(caId, certificateChain);

    return certificateChain;
  }
}
