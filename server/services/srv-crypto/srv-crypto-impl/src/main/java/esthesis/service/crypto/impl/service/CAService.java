package esthesis.service.crypto.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.exception.QCouldNotSaveException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QMutationNotPermittedException;
import esthesis.service.common.BaseService;
import esthesis.service.common.validation.CVException;
import esthesis.service.crypto.dto.CPPPemHolder;
import esthesis.service.crypto.dto.Ca;
import esthesis.service.crypto.dto.form.ImportCaForm;
import esthesis.service.crypto.dto.request.CertificateSignRequest;
import esthesis.service.crypto.dto.request.CreateCARequest;
import esthesis.service.crypto.dto.request.CreateCARequest.CreateCARequestBuilder;
import esthesis.service.crypto.dto.request.CreateKeyPairRequest;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.panache.common.Sort;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Certificate Authority management.
 */
@Slf4j
@ApplicationScoped
public class CAService extends BaseService<Ca> {

  @Inject
  @RestClient
  SettingsResource settingsResource;

  @Inject
  KeyService keyService;

  @Inject
  CertificateService certificateService;

  @Inject
  ObjectMapper mapper;

  public Ca save(Ca ca) {
    // CAs can not be edited, so throw an exception in that case.
    if (ca.getId() != null) {
      throw new QMutationNotPermittedException(
          "A CA can not be edited once created.");
    }

    // Check a CA with the same name doesn't already exist.
    if (findByColumn("cn", ca.getCn()) != null) {
      new CVException<Ca>()
          .addViolation("cn",
              "A Certificate Authority with CN '{}' already exists.",
              ca.getCn())
          .throwCVE();
    }

    // Create the CA.
    try {
      CreateCARequestBuilder createCARequestBuilder = CreateCARequest.builder()
          .createKeyPairRequest(CreateKeyPairRequest.builder()
              .keySize(settingsResource.findByName(
                  NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE).asInt())
              .keyPairGeneratorAlgorithm(settingsResource.findByName(
                  NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
              .build())
          .subjectCN(ca.getCn())
          .locale(Locale.US)
          .serial(BigInteger.valueOf(1))
          .signatureAlgorithm(
              settingsResource.findByName(
                      NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM)
                  .asString())
          .validFrom(Instant.now())
          .validTo(ca.getValidity());

      // If this CA has a parent CA (i.e. it is a sub-CA) fetch the details of the parent.
      if (ca.getParentCaId() != null) {
        Ca parentCa = findById(ca.getParentCaId());
        createCARequestBuilder
            .issuerCN(parentCa.getCn())
            .issuerPrivateKeyAlgorithm(settingsResource.findByName(
                NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
            .issuerPrivateKey(parentCa.getPrivateKey());
        ca.setParentCa(parentCa.getCn());
      }

      final CPPPemHolder cppPemHolderDTO = createCA(
          createCARequestBuilder.build());
      ca.setCertificate(cppPemHolderDTO.getCertificate());
      ca.setPrivateKey(cppPemHolderDTO.getPrivateKey());
      ca.setPublicKey(cppPemHolderDTO.getPublicKey());
      ca.setIssued(Instant.now());

      return super.save(ca);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException |
             OperatorCreationException | NoSuchProviderException e) {
      throw new QCouldNotSaveException("Could not save CA.", e);
    }
  }

  /**
   * Create a new Certificate Authority. This method also supports creating a
   * sub-CA by providing the issuer's information.
   *
   * @param createca the details of the CA to be created
   * @return the generated certificate
   * @throws NoSuchAlgorithmException  thrown when no algorithm is found for
   *                                   encryption
   * @throws InvalidKeySpecException   thrown when the provided key is invalid
   * @throws OperatorCreationException thrown when something unexpected happens
   *                                   during the encryption
   * @throws IOException               thrown when something unexpected happens
   */
  public CPPPemHolder createCA(final CreateCARequest createca)
  throws NoSuchAlgorithmException, InvalidKeySpecException,
         OperatorCreationException, IOException,
         NoSuchProviderException {
    log.debug("Creating a new CA '{}'.", createca);
    // Create a keypair for this CA.
    final KeyPair keyPair = keyService.createKeyPair(
        createca.getCreateKeyPairRequest());

    // Prepare signing.
    CertificateSignRequest certificateSignRequest = new CertificateSignRequest();
    certificateSignRequest.setValidForm(createca.getValidFrom());
    certificateSignRequest.setValidTo(createca.getValidTo());
    certificateSignRequest.setLocale(createca.getLocale());
    certificateSignRequest.setPublicKey(keyPair.getPublic());
    certificateSignRequest.setPrivateKey(keyPair.getPrivate());
    certificateSignRequest.setSignatureAlgorithm(
        createca.getSignatureAlgorithm());
    certificateSignRequest.setSubjectCN(createca.getSubjectCN());
    certificateSignRequest.setCa(true);

    // Choose which private key to use. If no parent key is found then this is a self-signed certificate and the
    // private key created for the keypair will be used.
    if (StringUtils.isNotEmpty(createca.getIssuerCN())
        && StringUtils.isNotEmpty(
        createca.getIssuerPrivateKey())) {
      certificateSignRequest.setIssuerPrivateKey(
          keyService.pemToPrivateKey(
              createca.getIssuerPrivateKey(),
              createca.getIssuerPrivateKeyAlgorithm()));
      certificateSignRequest.setIssuerCN(createca.getIssuerCN());
    } else {
      certificateSignRequest.setIssuerPrivateKey(keyPair.getPrivate());
      certificateSignRequest.setIssuerCN(createca.getSubjectCN());
    }

    final X509CertificateHolder certHolder = certificateService.generateCertificate(
        certificateSignRequest);

    // Prepare reply.
    final CPPPemHolder cppPemKey = new CPPPemHolder();
    cppPemKey.setPublicKey(keyService.publicKeyToPEM(
        keyPair.getPublic().getEncoded()));
    cppPemKey.setPrivateKey(keyService.privateKeyToPEM(
        keyPair.getPrivate().getEncoded()));
    cppPemKey.setCertificate(certificateService.certificateToPEM(certHolder));

    return cppPemKey;
  }

  public List<Ca> getEligibleForSigning() {
    return getRepository().find("privateKey != null", Sort.ascending("cn"))
        .list();
  }

  public Ca importCa(ImportCaForm importCaForm) {
    try {
      Ca ca = mapper.readValue(importCaForm.getBackup().uploadedFile().toFile(),
          Ca.class);
      if (findById(ca.getId()) != null
          || findByColumn("cn", ca.getCn()) != null) {
        throw new QAlreadyExistsException(
            "A CA with this CN or id already exists.");
      }

      super.getRepository().persistOrUpdate(ca);
      return ca;
    } catch (IOException e) {
      throw new QMismatchException("Could not import CA.", e);
    }
  }
}
