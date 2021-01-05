package esthesis.platform.backend.server.service;

import com.eurodyn.qlack.common.exception.QCouldNotSaveException;
import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QMutationNotPermittedException;
import com.eurodyn.qlack.fuse.crypto.dto.CertificateSignDTO;
import com.eurodyn.qlack.fuse.crypto.dto.CreateKeyPairDTO;
import com.eurodyn.qlack.fuse.crypto.service.CryptoAsymmetricService;
import com.eurodyn.qlack.fuse.crypto.service.CryptoCAService;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import esthesis.platform.backend.common.util.Base64E;
import esthesis.platform.backend.server.config.AppConstants.Cryptography.KeyType;
import esthesis.platform.backend.server.config.AppConstants.Cryptography.Type;
import esthesis.platform.backend.server.config.AppProperties;
import esthesis.platform.backend.server.config.AppSettings.Setting.Security;
import esthesis.platform.backend.server.dto.CertificateDTO;
import esthesis.platform.backend.server.dto.DownloadReply;
import esthesis.platform.backend.server.mapper.CertificateMapper;
import esthesis.platform.backend.server.model.Ca;
import esthesis.platform.backend.server.model.Certificate;
import esthesis.platform.backend.server.repository.CertificateRepository;
import javax.crypto.NoSuchPaddingException;
import lombok.RequiredArgsConstructor;
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
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class CertificatesService extends BaseService<CertificateDTO, Certificate> {

  private final CAService caService;
  private final AppProperties appProperties;
  private final SecurityService securityService;
  private final SettingResolverService settingResolverService;
  private final CryptoAsymmetricService cryptoAsymmetricService;
  private final CryptoCAService cryptoCAService;
  private final ObjectMapper objectMapper;
  private final CertificateMapper certificateMapper;
  private final CertificateRepository certificateRepository;

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

      return super.save(certificateDTO);
    } catch (NoSuchAlgorithmException | IOException | OperatorCreationException | InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchProviderException e) {
      throw new QCouldNotSaveException("Could not save certificate.", e);
    }
  }

  private DownloadReply getCertificateChain(DownloadReply keyDownloadReply, String caCN) {
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

  public DownloadReply download(long certificateId, int keyType, boolean base64)
    throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
    InvalidAlgorithmParameterException, IOException {
    final Certificate certificate = findEntityById(certificateId);

    DownloadReply keyDownloadReply = new DownloadReply();
    keyDownloadReply.setFilename(new Slugify().slugify(certificate.getCn()));

    switch (keyType) {
      case KeyType.CERTIFICATE:
        keyDownloadReply.setFilename(keyDownloadReply.getFilename() + ".crt");
        keyDownloadReply.setPayload(certificate.getCertificate());
        if (!certificate.getIssuer().equals(certificate.getCn()) && StringUtils
          .isNotBlank(caService.findEntityByCN(certificate.getIssuer()).getParentCa())) {
          getCertificateChain(keyDownloadReply,
            caService.findEntityByCN(certificate.getIssuer()).getCn());
        }
        break;

      case KeyType.PUBLIC_KEY:
        keyDownloadReply.setFilename(keyDownloadReply.getFilename() + ".pem");
        keyDownloadReply.setPayload(certificate.getPublicKey());
        break;
      case KeyType.PRIVATE_KEY:
        keyDownloadReply.setFilename(keyDownloadReply.getFilename() + ".key");
        keyDownloadReply.setPayload(
          new String((securityService.decrypt(certificate.getPrivateKey())),
            StandardCharsets.UTF_8));
        break;
      default:
        throw new QDoesNotExistException("Request key type {0} does not exist.", keyType);
    }

    if (base64) {
      keyDownloadReply.setPayload(
        Base64E.encode(keyDownloadReply.getPayload().getBytes(StandardCharsets.UTF_8)));
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

  public void restore(String backup)
    throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
    InvalidAlgorithmParameterException {
    // Create a local copy of the system's ObjectMapper in order to overwrite Access.READ_ONLY attributes of the
    // underlying object.
    ObjectMapper localObjectMapper = objectMapper.copy();
    localObjectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
    final CertificateDTO certificateDTO = localObjectMapper.readValue(backup, CertificateDTO.class);

    if (!Objects.equals(certificateDTO.getType(),Type.CERTIFICATE)) {
      throw new QCouldNotSaveException("Backup is not a certificate.");
    }

    certificateDTO.setPrivateKey(securityService.encrypt(certificateDTO.getPrivateKey()));

    certificateRepository.save(certificateMapper.map(certificateDTO));
  }

  public String backup(long id)
    throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
    InvalidAlgorithmParameterException {
    final CertificateDTO certificateDTO = findById(id);
    certificateDTO.setPrivateKey(new String(securityService.decrypt(certificateDTO.getPrivateKey()),
      StandardCharsets.UTF_8));
    certificateDTO.setType(Type.CERTIFICATE);

    return objectMapper.writeValueAsString(certificateDTO);
  }
}
