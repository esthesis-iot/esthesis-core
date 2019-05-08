package esthesis.platform.server.service;

import com.eurodyn.qlack.fuse.crypto.CryptoAsymmetricService;
import com.eurodyn.qlack.fuse.crypto.CryptoSymmetricService;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.extension.device.DeviceMessage;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.config.AppSettings.Setting.Security;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.IncomingEncryption;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.IncomingSignature;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.OutgoingEncryption;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.OutgoingSignature;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.repository.CertificateRepository;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

/**
 * A utility service to centralise all encryption/decryption functions without scattering keys
 * around in other services.
 */
@Service
public class SecurityService {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(SecurityService.class.getName());

  private final AppProperties appProperties;
  private final CryptoSymmetricService cryptoSymmetricService;
  private final CryptoAsymmetricService cryptoAsymmetricService;
  private final SettingResolverService srs;
  private final CertificateRepository certificateRepository;
  private final ObjectMapper objectMapper;

  public SecurityService(AppProperties appProperties,
    CryptoSymmetricService cryptoSymmetricService, CryptoAsymmetricService cryptoAsymmetricService,
    SettingResolverService srs, CertificateRepository certificateRepository,
    ObjectMapper objectMapper) {
    this.appProperties = appProperties;
    this.cryptoSymmetricService = cryptoSymmetricService;
    this.cryptoAsymmetricService = cryptoAsymmetricService;
    this.srs = srs;
    this.certificateRepository = certificateRepository;
    this.objectMapper = objectMapper;
  }

  /**
   * Convenience method to verify the signature and/or decrypt incoming messages from devices.
   */
  public <T> void processIncomingMessage(DeviceMessage msg, Class<T> payloadClass,
    DeviceDTO deviceDTO)
  throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
         IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException,
         InvalidKeySpecException, SignatureException {
    // Verify the signature.
    if (srs.is(Security.INCOMING_SIGNATURE, IncomingSignature.SIGNED) &&
      StringUtils.isBlank(msg.getSignature())) {
      throw new SecurityException("There is no signature to verify.");
    }
    if (StringUtils.isNotBlank(msg.getSignature())) {
      if (StringUtils.isNotBlank(msg.getEncryptedPayload())) {
        verifySignature(
          msg.getEncryptedPayload().getBytes(StandardCharsets.UTF_8),
          msg.getSignature(), deviceDTO);
      } else {
        verifySignature(objectMapper.writeValueAsBytes(msg.getPayload()),
          msg.getSignature(), deviceDTO);
      }
    }

    // Decrypt the payload.
    if (StringUtils.isNotBlank(msg.getEncryptedPayload()) && msg.getPayload() != null) {
      throw new IllegalStateException("An incoming message cannot have both encrypted and "
        + "non-encrypted payload.");
    }
    if (srs.is(Security.INCOMING_ENCRYPTION, IncomingEncryption.ENCRYPTED) &&
      StringUtils.isBlank(msg.getEncryptedPayload())) {
      throw new SecurityException("There is no encrypted payload to decrypt.");
    }
    if (StringUtils.isNotBlank(msg.getEncryptedPayload())) {
      byte[] plaintext =
        decrypt(Base64.decodeBase64(msg.getEncryptedPayload().getBytes(StandardCharsets.UTF_8)),
          deviceDTO);
      msg.setPayload(objectMapper.readValue(plaintext, payloadClass));
      msg.setEncryptedPayload(null);
    }
  }

  /**
   * Convenience method to sign and/or encrypt outgoing messages to devices.
   */
  public void prepareOutgoingMessage(DeviceMessage msg, DeviceDTO deviceDTO)
  throws JsonProcessingException, NoSuchPaddingException, InvalidAlgorithmParameterException,
         NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
         InvalidKeyException, InvalidKeySpecException, SignatureException {
    // Encrypt request if required.
    if (srs.is(Security.OUTGOING_ENCRYPTION, OutgoingEncryption.ENCRYPTED)) {
      msg.setEncryptedPayload(Base64.encodeBase64String(
        encrypt(objectMapper.writeValueAsBytes(msg.getPayload()), deviceDTO)));
      msg.setPayload(null);
    }

    // Sign if required.
    if (srs.is(Security.OUTGOING_SIGNATURE, OutgoingSignature.SIGNED)) {
      byte[] signature;
      if (StringUtils.isNotBlank(msg.getEncryptedPayload())) {
        signature = sign(msg.getEncryptedPayload().getBytes(StandardCharsets.UTF_8));
      } else {
        signature = sign(objectMapper.writeValueAsBytes(msg.getPayload()));
      }
      msg.setSignature(Base64.encodeBase64String(signature));
    }
  }

  /**
   * Signs a payload with the private key of the platform.
   *
   * @param payload The payload to sign.
   */
  public byte[] sign(byte[] payload)
  throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
         SignatureException, IllegalBlockSizeException, BadPaddingException,
         InvalidAlgorithmParameterException, NoSuchPaddingException {

    return cryptoAsymmetricService.sign(
      new String(decrypt(ReturnOptional.r(certificateRepository.findById(srs.getAsLong(Security.PLATFORM_CERTIFICATE)))
        .getPrivateKey()), StandardCharsets.UTF_8),
      payload,
      appProperties.getSecurityAsymmetricSignatureAlgorithm(),
      appProperties.getSecurityAsymmetricKeyAlgorithm());
  }

  /**
   * Verifies the signature of a payload.
   *
   * @param payload The payload to verify its signature.
   * @param signature The signature to verify.
   * @param deviceDTO The device to verify the signature for.
   */
  public void verifySignature(byte[] payload, String signature, DeviceDTO deviceDTO)
  throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
         SignatureException {

    if (!cryptoAsymmetricService.verifySignature(
      deviceDTO.getPublicKey(),
      payload,
      signature,
      appProperties.getSecurityAsymmetricSignatureAlgorithm(),
      appProperties.getSecurityAsymmetricKeyAlgorithm())) {
      throw new SecurityException("Signature validation failed.");
    }
  }

  /**
   * Encrypts a plaintext to be sent to a device using the shared session key which is retrieved
   * from the passed `hardwareId`.
   *
   * @param plaintext The plaintext to encrypt.
   * @param deviceDTO The device to encrypt for.
   */
  public byte[] encrypt(byte[] plaintext, DeviceDTO deviceDTO)
  throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
         BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    if (plaintext == null) {
      throw new IllegalStateException("No payload to encrypt.");
    }

    return cryptoSymmetricService.encrypt(
      plaintext,
      cryptoSymmetricService.keyFromString(
        deviceDTO.getSessionKey(),
        appProperties.getSecuritySymmetricKeyAlgorithm()),
      cryptoSymmetricService.generateIV(),
      appProperties.getSecuritySymmetricCipherAlgorithm(),
      appProperties.getSecuritySymmetricKeyAlgorithm(),
      true);
  }

  /**
   * Encrypts a plaintext with the platform's symmetric key.
   *
   * @param plaintext The plaintext to encrypt.
   * @return A Base64 encoded version of the ciphertext.
   */
  public String encrypt(byte[] plaintext)
  throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
         BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    return Base64.encodeBase64String(cryptoSymmetricService.encrypt(plaintext,
      cryptoSymmetricService
        .keyFromString(srs.get(Security.AES_KEY),
          appProperties.getSecuritySymmetricKeyAlgorithm()),
      srs.get(Security.AES_KEY).getBytes(StandardCharsets.UTF_8),
      appProperties.getSecuritySymmetricCipherAlgorithm(),
      appProperties.getSecurityAsymmetricKeyAlgorithm(),
      true));
  }

  /**
   * Encrypts a plaintext with the platform's symmetric key.
   *
   * @param plaintext The plaintext to encrypt.
   * @return A Base64 encoded version of the ciphertext.
   */
  public String encrypt(String plaintext)
  throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
         IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
    return encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decrypts a Base64 encoded ciphertext with the platform's symmetric key.
   *
   * @param ciphertext The ciphertext to decrypt.
   */
  public byte[] decrypt(byte[] ciphertext)
  throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
         BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    return cryptoSymmetricService.decrypt(Base64.decodeBase64(ciphertext),
      cryptoSymmetricService
        .keyFromString(srs.get(Security.AES_KEY),
          appProperties.getSecuritySymmetricKeyAlgorithm()),
      appProperties.getSecuritySymmetricCipherAlgorithm(),
      appProperties.getSecurityAsymmetricKeyAlgorithm());
  }

  /**
   * Decrypts a Base64 encoded ciphertext with the platform's symmetric key.
   *
   * @param ciphertext The ciphertext to decrypt.
   */
  public byte[] decrypt(String ciphertext)
  throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
         IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
    return decrypt(ciphertext.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decrypts an encrypted message coming from a device.
   *
   * @param cipherText The ciphertext to decrypt.
   * @param deviceDTO The device to decrypt for.
   */
  public byte[] decrypt(byte[] cipherText, DeviceDTO deviceDTO)
  throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
         BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    if (cipherText == null) {
      throw new IllegalStateException("No encrypted payload to decrypt.");
    }

    return cryptoSymmetricService.decrypt(
      cipherText,
      cryptoSymmetricService.keyFromString(
        deviceDTO.getSessionKey(),
        appProperties.getSecuritySymmetricKeyAlgorithm()),
      appProperties.getSecuritySymmetricCipherAlgorithm(),
      appProperties.getSecuritySymmetricKeyAlgorithm());
  }
}
