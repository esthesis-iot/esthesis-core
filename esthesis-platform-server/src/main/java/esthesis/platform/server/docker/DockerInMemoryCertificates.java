package esthesis.platform.server.docker;

import com.eurodyn.qlack.fuse.crypto.CryptoConversionService;
import com.google.common.base.Optional;
import com.spotify.docker.client.DockerCertificatesStore;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DockerInMemoryCertificates implements DockerCertificatesStore {

  private static final char[] KEY_STORE_PASSWORD = UUID.randomUUID().toString().toCharArray();
  private static final Logger log = LoggerFactory.getLogger(com.spotify.docker.client.DockerCertificates.class);
  private final SSLContext sslContext;
  private final CryptoConversionService cryptoConversionService = new CryptoConversionService();

  private DockerInMemoryCertificates(final DockerInMemoryCertificates.Builder builder)
      throws DockerCertificateException {
    if ((builder.caCert == null) || (builder.clientCert == null) || (builder.clientKey == null)) {
      throw new DockerCertificateException(
          "caCertPath, clientCertPath, and clientKeyPath must all be specified.");
    }

    try {
      final PrivateKey clientKey = cryptoConversionService.pemToPrivateKey(
          builder.clientKey, builder.securityProvider, builder.securityAlgorithm);

      final List<Certificate> clientCerts = Arrays.asList(cryptoConversionService.pemToCertificate(builder.clientCert));

      final KeyStore keyStore = newKeyStore();
      keyStore.setKeyEntry("key", clientKey, KEY_STORE_PASSWORD,
          clientCerts.toArray(new Certificate[clientCerts.size()]));

      final List<Certificate> caCerts = Arrays.asList(cryptoConversionService.pemToCertificate(builder.caCert));

      final KeyStore trustStore = newKeyStore();
      for (Certificate caCert : caCerts) {
        X509Certificate crt = (X509Certificate) caCert;
        String alias = crt.getSubjectX500Principal()
            .getName();
        trustStore.setCertificateEntry(alias, caCert);
      }

      this.sslContext = builder.sslContextFactory
          .newSslContext(keyStore, KEY_STORE_PASSWORD, trustStore);
    } catch (CertificateException | IOException | NoSuchAlgorithmException | InvalidKeySpecException | KeyStoreException | UnrecoverableKeyException | KeyManagementException | NoSuchProviderException e) {
      throw new DockerCertificateException(e);
    }
  }

  private KeyStore newKeyStore() throws CertificateException, NoSuchAlgorithmException,
      IOException, KeyStoreException {
    final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(null);
    return keyStore;
  }

  private List<Certificate> readCertificates(Path file) throws CertificateException, IOException {
    try (InputStream inputStream = Files.newInputStream(file)) {
      final CertificateFactory cf = CertificateFactory.getInstance("X.509");
      return new ArrayList<>(cf.generateCertificates(inputStream));
    }
  }

  public SSLContext sslContext() {
    return this.sslContext;
  }

  public HostnameVerifier hostnameVerifier() {
    return NoopHostnameVerifier.INSTANCE;
  }

  public interface SslContextFactory {

    SSLContext newSslContext(KeyStore keyStore, char[] keyPassword, KeyStore trustStore)
        throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException,
        KeyManagementException;
  }

  private static class DefaultSslContextFactory implements
      com.spotify.docker.client.DockerCertificates.SslContextFactory {

    @Override
    public SSLContext newSslContext(KeyStore keyStore, char[] keyPassword, KeyStore trustStore)
        throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException,
        KeyManagementException {
      return SSLContexts.custom()
          .loadTrustMaterial(trustStore, null)
          .loadKeyMaterial(keyStore, keyPassword)
          .build();
    }
  }

  public static DockerInMemoryCertificates.Builder builder() {
    return new DockerInMemoryCertificates.Builder();
  }

  public static class Builder {

    private com.spotify.docker.client.DockerCertificates.SslContextFactory sslContextFactory =
        new DockerInMemoryCertificates.DefaultSslContextFactory();
    private String caCert;
    private String clientKey;
    private String clientCert;
    private String securityProvider;
    private String securityAlgorithm;

    public DockerInMemoryCertificates.Builder caCert(final String caCert) {
      this.caCert = caCert;
      return this;
    }

    public DockerInMemoryCertificates.Builder clientKey(final String clientKey) {
      this.clientKey = clientKey;
      return this;
    }

    public DockerInMemoryCertificates.Builder clientCert(final String clientCert) {
      this.clientCert = clientCert;
      return this;
    }

    public DockerInMemoryCertificates.Builder securityAlgorithm(final String securityAlgorithm) {
      this.securityAlgorithm = securityAlgorithm;
      return this;
    }

    public DockerInMemoryCertificates.Builder securityProvider(final String securityProvider) {
      this.securityProvider = securityProvider;
      return this;
    }

    public DockerInMemoryCertificates.Builder sslFactory(
        final com.spotify.docker.client.DockerCertificates.SslContextFactory sslContextFactory) {
      this.sslContextFactory = sslContextFactory;
      return this;
    }

    public Optional<DockerCertificatesStore> build() throws DockerCertificateException {
      if (this.caCert == null || this.clientKey == null || this.clientCert == null) {
        log.debug("caCert, clientKey or clientCert not specified, not using SSL.");
        return Optional.absent();
      } else {
        return Optional.of((DockerCertificatesStore) new DockerInMemoryCertificates(this));
      }
    }
  }
}