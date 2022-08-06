package esthesis.service.crypto.impl.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;

@Slf4j
@ApplicationScoped
public class KeystoreService {

  @Inject
  KeyService keyService;

  /**
   * Creates an empty keystore. This keystore can later on be used to add keys
   * and certificates into it.
   *
   * @param keystoreType     The type of the keystore to create.
   * @param keystoreProvider The provider for the specific keystore type.
   * @param keystorePassword The password of the keystore.
   */
  public byte[] createKeystore(final String keystoreType,
      final String keystoreProvider, final String keystorePassword)
  throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException,
         IOException, CertificateException {
    // Create a new keystore.
    KeyStore ks;
    if (StringUtils.isBlank(keystoreType) || StringUtils
        .isBlank(keystoreProvider)) {
      ks = KeyStore.getInstance(KeyStore.getDefaultType());
    } else {
      ks = KeyStore.getInstance(keystoreType, keystoreProvider);
    }

    // Initialise the new keystore with user-provided password.
    if (StringUtils.isNotBlank(keystorePassword)) {
      ks.load(null, keystorePassword.toCharArray());
    } else {
      ks.load(null, null);
    }

    return keystoreToByteArray(ks, keystorePassword);
  }

  /**
   * Converts a {@link KeyStore} to a byte array.
   *
   * @param keystore         The keystore to convert.
   * @param keystorePassword The password of the keystore.
   */
  public byte[] keystoreToByteArray(final KeyStore keystore,
      final String keystorePassword)
  throws IOException, CertificateException, NoSuchAlgorithmException,
         KeyStoreException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos)) {
      keystore.store(bos, keystorePassword.toCharArray());
      return baos.toByteArray();
    }
  }

  /**
   * Converts a byte array representing a {@link KeyStore} to a KeyStore.
   *
   * @param keystore         The keystore representation as a byte array.
   * @param keystoreType     The type of the keystore, e.g. PKCS12
   * @param keystorePassword The password of the keystore.
   * @param keystoreProvider A provider for the specific keystore type.
   */
  public KeyStore keystoreFromByteArray(final byte[] keystore,
      final String keystoreType, final String keystorePassword,
      final String keystoreProvider)
  throws KeyStoreException, NoSuchProviderException, IOException,
         CertificateException,
         NoSuchAlgorithmException {
    final KeyStore ks;

    if (StringUtils.isBlank(keystoreType) || StringUtils
        .isBlank(keystoreProvider)) {
      ks = KeyStore.getInstance(KeyStore.getDefaultType());
    } else {
      ks = KeyStore.getInstance(keystoreType, keystoreProvider);
    }
    try (BufferedInputStream bis = new BufferedInputStream(
        new ByteArrayInputStream(keystore))) {
      if (StringUtils.isNotBlank(keystorePassword)) {
        ks.load(bis, keystorePassword.toCharArray());
      } else {
        ks.load(bis, null);
      }
    }

    return ks;
  }

  /**
   * Saves a certificate to the keystore. If the certificate identified by the
   * alias already exists it gets overwritten.
   *
   * @param keystore         The keystore to save the symmetric key into.
   * @param keystoreType     The type of the keystore.
   * @param keystoreProvider The provider for the specific type of keystore.
   * @param keystorePassword The password of the keystore.
   * @param certificate      The certificate to save.
   * @param certificateAlias The alias under which the certificate is saved.
   */
  public byte[] saveCertificate(final byte[] keystore,
      final String keystoreType, final String keystoreProvider,
      final String keystorePassword, final String certificateAlias,
      final byte[] certificate)
  throws NoSuchAlgorithmException, CertificateException,
         NoSuchProviderException, KeyStoreException,
         IOException {
    // Load the keystore.
    KeyStore ks = keystoreFromByteArray(keystore, keystoreType,
        keystorePassword, keystoreProvider);

    // Add the certificate.
    ks.setCertificateEntry(certificateAlias,
        new JcaX509CertificateConverter()
            .getCertificate(new X509CertificateHolder(certificate)));

    return keystoreToByteArray(ks, keystorePassword);
  }

  /**
   * Saves a private (asymmetric) key to the keystore. If the key identified by
   * the alias of the key already exists it gets overwritten.
   *
   * @param keystore         The keystore to save the symmetric key into.
   * @param keystoreType     The type of the keystore.
   * @param keystoreProvider The provider for the specific type of keystore.
   * @param keystorePassword The password of the keystore.
   * @param keyAlias         The alias under which the key will be saved.
   * @param key              The key to save in DER format.
   * @param keyAlgorithm     The algorithm the key was generated with.
   * @param keyProvider      The provider for the specific key algorithm.
   * @param keyPassword      The password of the key.
   * @param certificates     The certificate chain for the key.
   */
  @SuppressWarnings("squid:S00107")
  public byte[] savePrivateKey(final byte[] keystore,
      final String keystoreType,
      final String keystoreProvider, final String keystorePassword,
      final String keyAlias,
      final byte[] key, final String keyAlgorithm, final String keyProvider,
      final String keyPassword, final Set<byte[]> certificates)
  throws NoSuchAlgorithmException, CertificateException,
         NoSuchProviderException, KeyStoreException,
         IOException, InvalidKeySpecException {
    // Load the keystore.
    KeyStore ks = keystoreFromByteArray(keystore, keystoreType,
        keystorePassword, keystoreProvider);

    java.security.cert.Certificate[] certs = certificates.stream().map(cert -> {
      try {
        return new JcaX509CertificateConverter()
            .getCertificate(new X509CertificateHolder(cert));
      } catch (CertificateException | IOException e) {
        log.error("Could not read certificate.", e);
        return null;
      }
    }).toArray(java.security.cert.Certificate[]::new);

    // Add the key.
    final Key privateKey = keyService
        .privateKeyFromByteArray(key, keyAlgorithm, keyProvider);
    ks.setKeyEntry(keyAlias, privateKey,
        keyPassword != null ? keyPassword.toCharArray() : "".toCharArray(),
        certs);

    return keystoreToByteArray(ks, keystorePassword);
  }
}
