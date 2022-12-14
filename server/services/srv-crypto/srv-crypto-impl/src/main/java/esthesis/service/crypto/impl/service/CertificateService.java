package esthesis.service.crypto.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.exception.QCouldNotSaveException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QMutationNotPermittedException;
import esthesis.service.common.BaseService;
import esthesis.service.crypto.dto.CertificateSignRequestDTO;
import esthesis.service.crypto.dto.CreateKeyPairRequestDTO;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.form.ImportCertificateForm;
import esthesis.service.settings.resource.SettingsResource;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class CertificateService extends BaseService<CertificateEntity> {

  @Inject
  ObjectMapper mapper;

  @Inject
  CAService caService;

  @Inject
  CryptoService cryptoService;

  @Inject
  @RestClient
  SettingsResource settingsResource;


  public CertificateEntity importCertificate(
      ImportCertificateForm importCertificateForm) {
    try {
      CertificateEntity certificateEntity = mapper.readValue(
          importCertificateForm.getBackup().uploadedFile().toFile(),
          CertificateEntity.class);
      if (findById(certificateEntity.getId()) != null
          || findFirstByColumn("cn", certificateEntity.getCn()) != null) {
        throw new QAlreadyExistsException(
            "A certificate with this CN or id already exists.");
      }

      super.getRepository().persistOrUpdate(certificateEntity);

      return certificateEntity;
    } catch (IOException e) {
      throw new QMismatchException("Could not import certificate.", e);
    }
  }

  @Override
  public CertificateEntity save(CertificateEntity certificateEntity) {
    // Certificates can not be edited, so throw an exception in that case.
    if (certificateEntity.getId() != null) {
      throw new QMutationNotPermittedException(
          "A certificate can not be edited once created.");
    }

    try {
      // Get the issuer CA.
      CaEntity caEntity = null;
      if (StringUtils.isNotBlank(certificateEntity.getIssuer())) {
        caEntity = caService.findFirstByColumn("cn", certificateEntity.getIssuer());
      }

      // Generate a keypair.
      final KeyPair keyPair = cryptoService.createKeyPair(
          CreateKeyPairRequestDTO.builder()
              .keySize(settingsResource.findByName(
                  NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE).asInt())
              .keyPairGeneratorAlgorithm(
                  settingsResource.findByName(
                          NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM)
                      .asString())
              .build()
      );

      // Prepare the sign request.
      CertificateSignRequestDTO certificateSignRequestDTO = new CertificateSignRequestDTO();
      certificateSignRequestDTO
          .setLocale(Locale.US)
          .setPrivateKey(keyPair.getPrivate())
          .setPublicKey(keyPair.getPublic())
          .setSignatureAlgorithm(settingsResource.findByName(
              NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM).asString())
          .setSubjectCN(certificateEntity.getCn())
          .setValidForm(Instant.now())
          .setValidTo(certificateEntity.getValidity());

      if (StringUtils.isNotEmpty(certificateEntity.getSan())) {
        certificateSignRequestDTO.setSan(
            Arrays.stream(certificateEntity.getSan().split(",")).map(String::trim)
                .collect(Collectors.joining(","))
        );
      }

      if (caEntity != null) {
        certificateSignRequestDTO.setIssuerCN(caEntity.getCn());
        certificateSignRequestDTO.setIssuerPrivateKey(
            cryptoService.pemToPrivateKey(
                caEntity.getPrivateKey(),
                settingsResource.findByName(
                        NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM)
                    .asString()));
      } else {
        certificateSignRequestDTO.setIssuerCN(certificateEntity.getCn());
        certificateSignRequestDTO.setIssuerPrivateKey(keyPair.getPrivate());
      }

      // Sign the certificate.
      final X509CertificateHolder x509CertificateHolder =
          cryptoService.generateCertificate(certificateSignRequestDTO);

      // Populate the certificate DTO to persist it.
      certificateEntity.setIssued(certificateSignRequestDTO.getValidForm());
      certificateEntity
          .setPrivateKey(cryptoService.privateKeyToPEM(keyPair.getPrivate()));
      certificateEntity.setPublicKey(cryptoService.publicKeyToPEM(keyPair.getPublic()));
      certificateEntity.setIssuer(certificateSignRequestDTO.getIssuerCN());
      certificateEntity
          .setCertificate(cryptoService.certificateToPEM(x509CertificateHolder.toASN1Structure()));
      certificateEntity.setSan(certificateSignRequestDTO.getSan());

      return super.save(certificateEntity);
    } catch (NoSuchAlgorithmException | IOException |
             OperatorCreationException |
             InvalidKeySpecException | NoSuchProviderException e) {
      throw new QCouldNotSaveException("Could not save certificate.", e);
    }
  }

}
