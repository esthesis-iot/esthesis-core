package esthesis.platform.server.service;

import com.eurodyn.qlack.fuse.crypto.CryptoSymmetricService;
import com.eurodyn.qlack.util.data.encryption.EncryptorDecryptor;
import esthesis.platform.server.config.AppProperties;
import org.springframework.stereotype.Service;

/**
 * A utility service to centralise all encryption/decryption functions without scattering keys around in other
 * services.
 */
@Service
public class SecurityService implements EncryptorDecryptor {

  private final CryptoSymmetricService cryptoService;
  private final AppProperties appProperties;

  public SecurityService(CryptoSymmetricService cryptoService, AppProperties appProperties) {

    this.cryptoService = cryptoService;
    this.appProperties = appProperties;
  }

  /**
   * Encrypts a message.
   *
   * @param message The message to encrypt.
   * @return Returns a hexed version of the encrypted message.
   */
  @Override
  public String encrypt(String message) {
    return cryptoService
        .encrypt(message, appProperties.getSecuritySymmetricKey(), appProperties.getSecuritySymmetricKeySalt());
  }

  /**
   * Decrypts a message.
   *
   * @param ciphertext An encrypted message.
   * @return Returns the original, unencrypted message.
   */
  @Override
  public String decrypt(String ciphertext) {
    return cryptoService
        .decrypt(ciphertext, appProperties.getSecuritySymmetricKey(), appProperties.getSecuritySymmetricKeySalt());
  }
}
