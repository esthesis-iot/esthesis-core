package esthesis.platform.server.service;

import com.eurodyn.qlack.common.exception.QCouldNotSaveException;
import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QMutationNotPermittedException;
import com.eurodyn.qlack.fuse.crypto.CryptoConversionService;
import com.eurodyn.qlack.fuse.crypto.CryptoKeyService;
import com.eurodyn.qlack.fuse.crypto.CryptoSignService;
import com.eurodyn.qlack.fuse.crypto.dto.CreateKeyPairDTO;
import com.eurodyn.qlack.fuse.crypto.dto.SignDTO;
import com.github.slugify.Slugify;
import esthesis.platform.server.config.AppConstants.Audit;
import esthesis.platform.server.config.AppConstants.Cryptography.KeyType;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.CertificateDTO;
import esthesis.platform.server.dto.KeyDownloadReply;
import esthesis.platform.server.model.Ca;
import esthesis.platform.server.model.Certificate;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@Service
@Validated
@Transactional
public class CertificatesService extends BaseService<CertificateDTO, Certificate> {

  private final AuditServiceProxy auditService;
  private final CryptoSignService cryptoSignService;
  private final CAService caService;
  private final CryptoKeyService cryptoKeyService;
  private final AppProperties appProperties;
  private final SecurityService securityService;
  private final CryptoConversionService cryptoConversionService;

  public CertificatesService(AuditServiceProxy auditService,
      CryptoSignService cryptoSignService, CAService caService,
      CryptoKeyService cryptoKeyService, AppProperties appProperties,
      SecurityService securityService, CryptoConversionService cryptoConversionService) {
    this.auditService = auditService;
    this.cryptoSignService = cryptoSignService;
    this.caService = caService;
    this.cryptoKeyService = cryptoKeyService;
    this.appProperties = appProperties;
    this.securityService = securityService;
    this.cryptoConversionService = cryptoConversionService;
  }

  @Override
  public CertificateDTO save(CertificateDTO certificateDTO) {
    // Certificates can not be edited, so throw \an exception in that case.
    if (certificateDTO.getId() != null) {
      throw new QMutationNotPermittedException("A certificate can not be edited once created.");
    }

    try {
      // Get the issuer CA.
      Ca ca = null;
      if (StringUtils.isNotBlank(certificateDTO.getIssuer())) {
        ca = caService.findEntityByCN(certificateDTO.getIssuer());
      }

      // Generate a keypair.
      final KeyPair keyPair = cryptoKeyService.createKeyPair(CreateKeyPairDTO.builder()
          .keySize(appProperties.getSecurityCaKeypairKeySize())
          .generatorAlgorithm(appProperties.getSecurityCaKeypairGeneratorAlgorithm())
          .generatorProvider(appProperties.getSecurityCaKeypairGeneratorProvider())
          .secretAlgorithm(appProperties.getSecurityCaKeypairSecrectAlgorithm())
          .secretProvider(appProperties.getSecurityCaKeypairSecrectProvider())
          .build()
      );

      // Prepare the sign request.
      SignDTO signDTO = new SignDTO();
      signDTO
          .setLocale(Locale.US)
          .setPrivateKey(keyPair.getPrivate())
          .setPublicKey(keyPair.getPublic())
          .setSignatureProvider(appProperties.getSecurityCaKeypairSignatureProvider())
          .setSignatureAlgorithm(appProperties.getSecurityCaKeypairSignatureAlgorithm())
          .setSubjectCN(certificateDTO.getCn())
          .setValidForm(Instant.now())
          .setValidTo(certificateDTO.getValidity());

      if (ca != null) {
        signDTO.setIssuerCN(ca.getCn());
        signDTO.setIssuerPrivateKey(cryptoConversionService.pemToPrivateKey(securityService.decrypt(ca.getPrivateKey()),
            appProperties.getSecurityCaKeypairGeneratorProvider(),
            appProperties.getSecurityCaKeypairGeneratorAlgorithm()));
      } else {
        signDTO.setIssuerCN(certificateDTO.getCn());
        signDTO.setIssuerPrivateKey(keyPair.getPrivate());
      }

      // Sign the certificate.
      final X509CertificateHolder x509CertificateHolder = cryptoSignService.signKey(signDTO);

      // Populate the certificate DTO to persist it.
      certificateDTO.setIssued(signDTO.getValidForm());
      certificateDTO.setPrivateKey(securityService.encrypt(cryptoConversionService.privateKeyToPEM(keyPair)));
      certificateDTO.setPublicKey(cryptoConversionService.publicKeyToPEM(keyPair));
      certificateDTO.setIssuer(signDTO.getIssuerCN());
      certificateDTO.setCertificate(cryptoConversionService.certificateToPEM(x509CertificateHolder));

      // Audit.
      auditService.info(Audit.EVENT_CERTS, MessageFormat.format("Certificate {0} created.", certificateDTO.getCn()));

      return super.save(certificateDTO);
    } catch (NoSuchProviderException | NoSuchAlgorithmException | IOException | OperatorCreationException | InvalidKeySpecException e) {
      throw new QCouldNotSaveException("Could not save certificate.", e);
    }
  }

  private KeyDownloadReply getCertificateChain(KeyDownloadReply keyDownloadReply, String caCN) {
    try {
      Ca ca = caService.findEntityByCN(caCN);
      if (ca.getParentCa() != null && caService.findEntityByCN(ca.getParentCa()) != null) {
        return getCertificateChain(keyDownloadReply, ca.getParentCa());
      } else {
        keyDownloadReply.setPayload(ca.getCertificate() + keyDownloadReply.getPayload());
      }
    } catch (QDoesNotExistException e) {
      // Ignore.
    }

    return keyDownloadReply;
  }

  public KeyDownloadReply download(long certificateId, int keyType, boolean base64) {
    KeyDownloadReply keyDownloadReply = new KeyDownloadReply();
    final CertificateDTO certificateDTO = findById(certificateId);
    keyDownloadReply.setFilename(new Slugify().slugify(certificateDTO.getCn()));

    switch (keyType) {
      case KeyType.CERTIFICATE:
        keyDownloadReply.setFilename(keyDownloadReply.getFilename() + ".crt");
        keyDownloadReply.setPayload(certificateDTO.getCertificate());
        if (!certificateDTO.getIssuer().equals(certificateDTO.getCn()) && StringUtils
            .isNotBlank(caService.findEntityByCN(certificateDTO.getIssuer()).getParentCa())) {
          getCertificateChain(keyDownloadReply, caService.findEntityByCN(certificateDTO.getIssuer()).getCn());
        }
        break;
      case KeyType.PUBLIC_KEY:
        keyDownloadReply.setFilename(keyDownloadReply.getFilename() + ".pem");
        keyDownloadReply.setPayload(certificateDTO.getPublicKey());
        break;
      case KeyType.PRIVATE_KEY:
        keyDownloadReply.setFilename(keyDownloadReply.getFilename() + ".key");
        keyDownloadReply.setPayload(securityService.decrypt(certificateDTO.getPrivateKey()));
        break;
    }

    if (base64) {
      keyDownloadReply.setPayload(
          Base64.getEncoder().encodeToString(keyDownloadReply.getPayload().getBytes(StandardCharsets.UTF_8)));
      keyDownloadReply.setFilename(keyDownloadReply.getFilename() + ".base64");
    }

    return keyDownloadReply;
  }

}
