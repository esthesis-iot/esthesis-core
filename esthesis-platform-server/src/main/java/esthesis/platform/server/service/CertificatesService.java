package esthesis.platform.server.service;

import com.eurodyn.qlack.common.exception.QCouldNotSaveException;
import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QMutationNotPermittedException;
import com.eurodyn.qlack.fuse.crypto.CryptoAsymmetricService;
import com.eurodyn.qlack.fuse.crypto.CryptoCAService;
import com.eurodyn.qlack.fuse.crypto.dto.CertificateSignDTO;
import com.eurodyn.qlack.fuse.crypto.dto.CreateKeyPairDTO;
import com.github.slugify.Slugify;
import esthesis.platform.server.config.AppConstants.Audit;
import esthesis.platform.server.config.AppConstants.Cryptography.KeyType;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.config.AppSettings.Setting.Security;
import esthesis.platform.server.dto.CertificateDTO;
import esthesis.platform.server.dto.KeyDownloadReply;
import esthesis.platform.server.model.Ca;
import esthesis.platform.server.model.Certificate;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Locale;

@Service
@Validated
@Transactional
public class CertificatesService extends BaseService<CertificateDTO, Certificate> {

  private final AuditServiceProxy auditService;
  private final CAService caService;
  private final AppProperties appProperties;
  private final SecurityService securityService;
  private final SettingResolverService settingResolverService;
  private final CryptoAsymmetricService cryptoAsymmetricService;
  private final CryptoCAService cryptoCAService;

  public CertificatesService(AuditServiceProxy auditService, CAService caService,
    AppProperties appProperties, SecurityService securityService,
    SettingResolverService settingResolverService,
    CryptoAsymmetricService cryptoAsymmetricService,
    CryptoCAService cryptoCAService) {
    this.auditService = auditService;
    this.caService = caService;
    this.appProperties = appProperties;
    this.securityService = securityService;
    this.settingResolverService = settingResolverService;
    this.cryptoAsymmetricService = cryptoAsymmetricService;
    this.cryptoCAService = cryptoCAService;
  }

  @Override
  public CertificateDTO save(CertificateDTO certificateDTO) {
    // Certificates can not be edited, so throw an exception in that case.
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
      final KeyPair keyPair = cryptoAsymmetricService.createKeyPair(CreateKeyPairDTO.builder()
        .keySize(appProperties.getSecurityAsymmetricKeySize())
        .keyPairGeneratorAlgorithm(appProperties.getSecurityAsymmetricKeyAlgorithm())
        .build()
      );

      // Prepare the sign request.
      CertificateSignDTO signDTO = new CertificateSignDTO();
      signDTO
        .setLocale(Locale.US)
        .setPrivateKey(keyPair.getPrivate())
        .setPublicKey(keyPair.getPublic())
        .setSignatureAlgorithm(appProperties.getSecurityAsymmetricSignatureAlgorithm())
        .setSubjectCN(certificateDTO.getCn())
        .setValidForm(Instant.now())
        .setValidTo(certificateDTO.getValidity());

      if (ca != null) {
        signDTO.setIssuerCN(ca.getCn());
        signDTO.setIssuerPrivateKey(
          cryptoAsymmetricService.pemToPrivateKey(
            new String(securityService.decrypt(ca.getPrivateKey()), StandardCharsets.UTF_8),
            appProperties.getSecurityAsymmetricKeyAlgorithm()));
      } else {
        signDTO.setIssuerCN(certificateDTO.getCn());
        signDTO.setIssuerPrivateKey(keyPair.getPrivate());
      }

      // Sign the certificate.
      final X509CertificateHolder x509CertificateHolder = cryptoCAService
        .generateCertificate(signDTO);

      // Populate the certificate DTO to persist it.
      certificateDTO.setIssued(signDTO.getValidForm());
      certificateDTO
        .setPrivateKey(securityService.encrypt(cryptoAsymmetricService.privateKeyToPEM(keyPair)));
      certificateDTO.setPublicKey(cryptoAsymmetricService.publicKeyToPEM(keyPair));
      certificateDTO.setIssuer(signDTO.getIssuerCN());
      certificateDTO
        .setCertificate(cryptoCAService.certificateToPEM(x509CertificateHolder));

      // Audit.
      auditService.info(Audit.EVENT_CERTS,
        MessageFormat.format("Certificate {0} created.", certificateDTO.getCn()));

      return super.save(certificateDTO);
    } catch (NoSuchAlgorithmException | IOException | OperatorCreationException |
      InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException |
      InvalidKeyException e) {
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

  public KeyDownloadReply download(long certificateId, int keyType, boolean base64)
  throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
         IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
         IOException {
    KeyDownloadReply keyDownloadReply = new KeyDownloadReply();
    final CertificateDTO certificateDTO = findById(certificateId);
    keyDownloadReply.setFilename(new Slugify().slugify(certificateDTO.getCn()));

    switch (keyType) {
      case KeyType.CERTIFICATE:
        keyDownloadReply.setFilename(keyDownloadReply.getFilename() + ".crt");
        keyDownloadReply.setPayload(certificateDTO.getCertificate());
        if (!certificateDTO.getIssuer().equals(certificateDTO.getCn()) && StringUtils
          .isNotBlank(caService.findEntityByCN(certificateDTO.getIssuer()).getParentCa())) {
          getCertificateChain(keyDownloadReply,
            caService.findEntityByCN(certificateDTO.getIssuer()).getCn());
        }
        break;
      case KeyType.PUBLIC_KEY:
        keyDownloadReply.setFilename(keyDownloadReply.getFilename() + ".pem");
        keyDownloadReply.setPayload(certificateDTO.getPublicKey());
        break;
      case KeyType.PRIVATE_KEY:
        keyDownloadReply.setFilename(keyDownloadReply.getFilename() + ".key");
        keyDownloadReply.setPayload(
          new String((securityService.decrypt(certificateDTO.getPrivateKey())),
            StandardCharsets.UTF_8));
        break;
    }

    if (base64) {
      keyDownloadReply.setPayload(
        Base64.encodeBase64String(keyDownloadReply.getPayload().getBytes(StandardCharsets.UTF_8)));
      keyDownloadReply.setFilename(keyDownloadReply.getFilename() + ".base64");
    }

    return keyDownloadReply;
  }

  /**
   * A helper method to return the public key of the platform as defined in settings.
   */
  public String getPSPublicKey() {
    return findEntityById(settingResolverService.getAsLong(Security.PLATFORM_CERTIFICATE))
      .getPublicKey();

  }

  /**
   * A helper method to return the private key of the platform as defined in settings.
   *
   * @return Returns a decrypted packageVersion of the private key.
   */
  public String getPSPrivateKey()
  throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
         InvalidAlgorithmParameterException, IOException {
    return new String(securityService.decrypt(
      findEntityById(settingResolverService.getAsLong(Security.PLATFORM_CERTIFICATE))
        .getPrivateKey()), StandardCharsets.UTF_8);
  }
}
