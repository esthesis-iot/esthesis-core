package esthesis.device.runtime.util;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QSecurityException;
import com.eurodyn.qlack.fuse.crypto.service.CryptoAsymmetricService;
import com.eurodyn.qlack.fuse.crypto.service.CryptoSymmetricService;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.dto.DeviceMessage;
import esthesis.common.util.Base64E;
import esthesis.device.runtime.config.AppConstants.Mqtt.EventType;
import esthesis.device.runtime.config.AppProperties;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Validated
public class DeviceMessageUtil {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DeviceMessageUtil.class.getName());

  private final CryptoSymmetricService cryptoSymmetricService;
  private final CryptoAsymmetricService cryptoAsymmetricService;
  private final SecurityUtil securityUtil;
  private final AppProperties appProperties;
  private final ObjectMapper objectMapper;

  public DeviceMessageUtil(
    CryptoSymmetricService cryptoSymmetricService,
    CryptoAsymmetricService cryptoAsymmetricService,
    SecurityUtil securityUtil, AppProperties appProperties,
    ObjectMapper objectMapper) {
    this.cryptoSymmetricService = cryptoSymmetricService;
    this.cryptoAsymmetricService = cryptoAsymmetricService;
    this.securityUtil = securityUtil;
    this.appProperties = appProperties;
    this.objectMapper = objectMapper;
  }

  /**
   * Convenience method to verify the signature and/or decrypt incoming messages.
   */
  public <T> void processIncoming(DeviceMessage msg, Class<T> payloadClass) {
    // Verify the signature of the response if requested.
    if (appProperties.isIncomingSigned()) {
      if (StringUtils.isBlank(msg.getSignature())) {
        throw new SecurityException("There is no signature to verify.");
      }
      if (!securityUtil.isPSPublicKeyPresent()) {
        throw new SecurityException("The platform's public key is not available on the device to "
          + "verifySignature the signature.");
      }
      try {
        verifySignature(msg);
      } catch (IOException | SignatureException | InvalidKeyException | InvalidKeySpecException
        | NoSuchAlgorithmException e) {
        throw new SecurityException("Could not verify the signature.", e);
      }
    }

    // Decrypt the response if encrypted.
    if (appProperties.isIncomingEncrypted()) {
      if (StringUtils.isBlank(msg.getEncryptedPayload())) {
        throw new QSecurityException("There is no encrypted payload to decrypt.");
      }
      if (StringUtils.isBlank(securityUtil.getPrivateKey())) {
        throw new SecurityException("Incoming response is encrypted, however the device's private "
          + "key is not available.");
      }
      try {
        decrypt(msg, payloadClass);
      } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
        InvalidAlgorithmParameterException | IOException e) {
        throw new SecurityException("Could not decrypt message.", e);
      }
    }
  }

  /**
   * Convenience method to sign and/or encrypt outgoing messages.
   */
  public void prepareOutgoing(DeviceMessage msg) {
    // Encrypt request if required.
    if (appProperties.isOutgoingEncrypted()) {
      try {
        encrypt(msg);
      } catch (NoSuchPaddingException | IOException | NoSuchAlgorithmException |
        InvalidKeyException | InvalidAlgorithmParameterException e) {
        throw new SecurityException("Could not encrypt outgoing message.", e);
      }
    }

    // If public and private keys exist sign the request if requested so.
    if (appProperties.isOutgoingSigned()) {
      try {
        sign(msg);
      } catch (IOException | SignatureException | InvalidKeyException | InvalidKeySpecException
        | NoSuchAlgorithmException e) {
        throw new SecurityException("Could not sign outgoing message.", e);
      }
    }
  }

  public void sign(DeviceMessage msg)
  throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
         InvalidKeyException, SignatureException {
    if (msg.getPayload() != null && msg.getEncryptedPayload() != null) {
      throw new IllegalStateException(
        "Encrypted and non-encrypted payload can not be set simultaneously.");
    } else if (msg.getPayload() == null && msg.getEncryptedPayload() == null) {
      throw new IllegalStateException("No payload nor encrypted payload available to sign.");
    }

    if (StringUtils.isNotBlank(msg.getEncryptedPayload())) {
      msg.setSignature(Base64E.encode(cryptoAsymmetricService.sign(
        securityUtil.getPrivateKey(),
        msg.getEncryptedPayload().getBytes(StandardCharsets.UTF_8),
        appProperties.getSignatureAlgorithm(),
        appProperties.getAsymmetricKeyAlgorithm())));
    } else {
      msg.setSignature(Base64E.encode(cryptoAsymmetricService.sign(
        securityUtil.getPrivateKey(),
        objectMapper.writeValueAsBytes(msg.getPayload()),
        appProperties.getSignatureAlgorithm(),
        appProperties.getAsymmetricKeyAlgorithm())));
    }
  }

  public void verifySignature(DeviceMessage msg)
  throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
         SignatureException {
    if (msg.getSignature() == null) {
      throw new IllegalStateException("No signature available to verify signature.");
    }
    if (msg.getPayload() == null && msg.getEncryptedPayload() == null) {
      throw new IllegalStateException("No payload available to verify signature.");
    }

    if (msg.getPayload() != null) {
      if (!cryptoAsymmetricService.verifySignature(
        securityUtil.getPSPublicKey(),
        objectMapper.writeValueAsBytes(msg.getPayload()),
        msg.getSignature(),
        appProperties.getSignatureAlgorithm(),
        appProperties.getAsymmetricKeyAlgorithm())) {
        throw new SecurityException("Signature validation failed.");
      }
    } else {
      if (!cryptoAsymmetricService.verifySignature(
        securityUtil.getPSPublicKey(),
        msg.getEncryptedPayload().getBytes(StandardCharsets.UTF_8),
        msg.getSignature(),
        appProperties.getSignatureAlgorithm(),
        appProperties.getAsymmetricKeyAlgorithm())) {
        throw new SecurityException("Signature validation failed.");
      }
    }
  }

  public void verifySignature(Path path, String signature)
  throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException,
         SignatureException {
    LOGGER.log(Level.FINE, "Verifying signature {0} for file {1}.", new Object[]{signature,
      path.toFile().getAbsolutePath()});
    StopWatch stopWatch = StopWatch.createStarted();

    try (FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
      if (!cryptoAsymmetricService.verifySignature(
        securityUtil.getPSPublicKey(),
        fileInputStream,
        signature,
        appProperties.getSignatureAlgorithm(),
        appProperties.getAsymmetricKeyAlgorithm())) {
        throw new SecurityException(MessageFormat.format("Signature validation failed for {0}.",
          path.toFile().getAbsolutePath()));
      }
    }

    LOGGER.log(Level.FINE, "Signature verification of {0} took {1} msec.",
      new Object[]{path.toFile().getAbsolutePath(), stopWatch.getTime()});
  }


  public void encrypt(DeviceMessage msg)
  throws NoSuchPaddingException, IOException, NoSuchAlgorithmException, InvalidKeyException,
         InvalidAlgorithmParameterException {
    if (msg.getPayload() == null) {
      throw new IllegalStateException("No payload to encrypt.");
    }
    msg.setEncryptedPayload(
      Base64E.encode(cryptoSymmetricService.encrypt(
        objectMapper.writeValueAsBytes(msg.getPayload()),
        cryptoSymmetricService
          .keyFromString(securityUtil.getSessionKey(), appProperties.getSymmetricKeyAlgorithm()),
        appProperties.getSymmetricCipher(),
        appProperties.getSymmetricKeyAlgorithm())));
    //noinspection unchecked
    msg.setPayload(null);
  }

  /**
   * Decrypts the payload of a message using the session key established with the platform.
   * @param msg
   * @param payloadClass
   * @param <T>
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws InvalidAlgorithmParameterException
   * @throws IOException
   */
  public <T> void decrypt(DeviceMessage msg, Class<T> payloadClass)
  throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
         InvalidAlgorithmParameterException, IOException {
    if (msg.getEncryptedPayload() == null) {
      throw new IllegalStateException("No encrypted payload to decrypt.");
    }
    msg.setPayload(objectMapper.readValue(
      cryptoSymmetricService.decrypt(
        Base64E.decode(msg.getEncryptedPayload()),
        cryptoSymmetricService
          .keyFromString(securityUtil.getSessionKey(), appProperties.getSymmetricKeyAlgorithm()),
        appProperties.getSymmetricCipher(),
        appProperties.getSymmetricKeyAlgorithm()
      ), payloadClass));
    msg.setEncryptedPayload(null);
  }

  /**
   * Decrypts a file using the session key established with the platform.
   * @param path
   * @param deleteEncrypted Whether to delete the encrypted file after successful decryption or not.
   * @return Returns a full path to the decrypted file.
   * @throws IOException
   * @throws InvalidAlgorithmParameterException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws NoSuchPaddingException
   */
  public String decrypt(Path path, boolean deleteEncrypted)
  throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
         InvalidKeyException, NoSuchPaddingException {
    StopWatch stopWatch = StopWatch.createStarted();
    final Path decryptedFile = Paths.get(path.toFile().getAbsolutePath() + ".decrypted");
    Files.deleteIfExists(decryptedFile);
    LOGGER.log(Level.FINEST, "Decrypting {0} to {1}.",
      new Object[]{path.toFile().getAbsolutePath(), decryptedFile.toFile().getAbsolutePath()});
    try (FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
      try (FileOutputStream fileOutputStream = new FileOutputStream(decryptedFile.toFile())) {
        cryptoSymmetricService.decrypt(
          fileInputStream,
          fileOutputStream,
          cryptoSymmetricService
            .keyFromString(securityUtil.getProvisioningKey(), appProperties.getSymmetricKeyAlgorithm()),
          null,
          appProperties.getSymmetricCipher(),
          appProperties.getSymmetricKeyAlgorithm()
        );
      }
    }

    if (deleteEncrypted) {
      Files.delete(path);
    }
    LOGGER.log(Level.FINE, "Decryption of {0} took {1} msec.",
      new Object[]{path.toFile().getAbsolutePath(), stopWatch.getTime()});
    return decryptedFile.toFile().getAbsolutePath();
  }

  /**
   * Resolves a text-based topic name to its enum equivalent. If the topic name is not one of the
   * supported ones an exception is thrown. The CONTROL_REQUEST topic is not supported as this is
   * a topic where message are not originated from the agent.
   * @param topic The name of the topic to resolve.
   */
  public EventType resolveTopic(String topic) {
    if (appProperties.getTopicPing().equals(topic)) {
      return EventType.PING;
    } else if (appProperties.getTopicMetadata().equals(topic)) {
      return EventType.METADATA;
    } else if (appProperties.getTopicTelemetry().equals(topic)) {
      return EventType.TELEMETRY;
    } else if (appProperties.getTopicControlReply().equals(topic)) {
      return EventType.CONTROL_REPLY;
    } else {
      throw new QDoesNotExistException("Unsupported MQTT topic {0}.", topic);
    }
  }
}
