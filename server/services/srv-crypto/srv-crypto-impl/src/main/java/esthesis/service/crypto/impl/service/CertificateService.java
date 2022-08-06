package esthesis.service.crypto.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.AppConstants.Registry;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.exception.QCouldNotSaveException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QMutationNotPermittedException;
import esthesis.common.service.BaseService;
import esthesis.service.crypto.dto.Ca;
import esthesis.service.crypto.dto.Certificate;
import esthesis.service.crypto.dto.form.ImportCertificateForm;
import esthesis.service.crypto.dto.request.CertificateSignRequest;
import esthesis.service.crypto.dto.request.CreateKeyPairRequest;
import esthesis.service.registry.resource.RegistryResourceV1;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class CertificateService extends BaseService<Certificate> {

  private static final String CN = "CN";
  private static final String CERTIFICATE = "CERTIFICATE";
  private final String IPV4_PATTERN = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
  private final Pattern ipv4Pattern = Pattern.compile(IPV4_PATTERN);

  @Inject
  ObjectMapper mapper;

  @Inject
  CAService caService;

  @Inject
  KeyService keyService;

  @Inject
  @RestClient
  RegistryResourceV1 registryResourceV1;

  private boolean isValidIPV4Address(final String email) {
    Matcher matcher = ipv4Pattern.matcher(email);
    return matcher.matches();
  }

  /**
   * Signs a key with another key providing a certificate.
   *
   * @param certificateSignRequest the details of the signing to take place
   * @return the generated signature
   * @throws OperatorCreationException thrown when something unexpected happens
   *                                   during the encryption
   * @throws CertIOException           thrown when something unexpected happens
   *                                   while generating the certificate
   */
  @SuppressWarnings({"squid:S2274", "squid:S2142"})
  public X509CertificateHolder generateCertificate(
      final CertificateSignRequest certificateSignRequest)
  throws OperatorCreationException, CertIOException {
    log.trace("Generating a certificate for '{}'.", certificateSignRequest);
    // Create a generator for the certificate including all certificate details.
    final X509v3CertificateBuilder certGenerator;
    // Synchronize this part, so that no two certificates can be created with the same timestamp.

    synchronized (this) {
      certGenerator = new X509v3CertificateBuilder(new X500Name(
          CN + "=" + StringUtils.defaultIfBlank(
              certificateSignRequest.getIssuerCN(),
              certificateSignRequest.getSubjectCN())),
          certificateSignRequest.isCa() ? BigInteger.ONE
              : BigInteger.valueOf(Instant.now().toEpochMilli()),
          new Date(certificateSignRequest.getValidForm().toEpochMilli()),
          new Date(certificateSignRequest.getValidTo().toEpochMilli()),
          certificateSignRequest.getLocale(),
          new X500Name(CN + "=" + certificateSignRequest.getSubjectCN()),
          SubjectPublicKeyInfo.getInstance(
              certificateSignRequest.getPublicKey().getEncoded()));
    }

    // Add SANs.
    if (StringUtils.isNotEmpty(certificateSignRequest.getSan())) {
      GeneralNames subjectAltNames = new GeneralNames(
          Arrays.stream(certificateSignRequest.getSan().split(","))
              .map(String::trim).map(s -> {
                if (isValidIPV4Address(s)) {
                  return new GeneralName(GeneralName.iPAddress, s);
                } else {
                  return new GeneralName(GeneralName.dNSName, s);
                }
              }).toArray(GeneralName[]::new));
      certGenerator.addExtension(Extension.subjectAlternativeName, false,
          subjectAltNames);
    }

    // Check if this is a CA certificate and in that case add the necessary key extensions.
    if (certificateSignRequest.isCa()) {
      certGenerator.addExtension(Extension.basicConstraints, true,
          new BasicConstraints(true));
      certGenerator.addExtension(Extension.keyUsage, true,
          new KeyUsage(KeyUsage.cRLSign | KeyUsage.keyCertSign));
    } else {
      certGenerator.addExtension(Extension.basicConstraints, true,
          new BasicConstraints(false));
    }

    // Generate the certificate.
    final X509CertificateHolder certHolder;
    certHolder = certGenerator.build(
        new JcaContentSignerBuilder(
            certificateSignRequest.getSignatureAlgorithm()).build(
            certificateSignRequest.getIssuerPrivateKey()));

    return certHolder;
  }

  /**
   * Converts a certificate to a PEM format encoded as X.509.
   *
   * @param certificateHolder the certificate to convert
   * @return the generated PEM
   * @throws IOException thrown when something unexpected happens
   */
  public String certificateToPEM(final X509CertificateHolder certificateHolder)
  throws IOException {
    log.debug("Converting '{}' certificate to PEM.", certificateHolder);
    try (StringWriter pemStrWriter = new StringWriter()) {
      try (PemWriter writer = new PemWriter(pemStrWriter)) {
        writer.writeObject(
            new PemObject(CERTIFICATE, certificateHolder.getEncoded()));
        writer.flush();
        return pemStrWriter.toString();
      }
    }
  }

  /**
   * Parses a certificate in PEM format encoded as X.509.
   *
   * @param cert the certificate in PEM format
   * @return the generated certificate
   * @throws CertificateException thrown when something unexpected happens while
   *                              generating the certificate
   */
  public X509Certificate pemToCertificate(final String cert)
  throws CertificateException {
    log.trace("Parsing '{}' PEM certificate.", cert);
    CertificateFactory fact = CertificateFactory.getInstance("X.509");

    return (X509Certificate) fact.generateCertificate(
        new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
  }

  public Certificate importCertificate(
      ImportCertificateForm importCertificateForm) {
    try {
      Certificate certificate = mapper.readValue(
          importCertificateForm.getBackup().uploadedFile().toFile(),
          Certificate.class);
      if (findById(certificate.getId()) != null
          || findByColumn("cn", certificate.getCn()) != null) {
        throw new QAlreadyExistsException(
            "A certificate with this CN or id already exists.");
      }

      super.getRepository().persistOrUpdate(certificate);
      return certificate;
    } catch (IOException e) {
      throw new QMismatchException("Could not import certificate.", e);
    }
  }

  @Override
  public Certificate save(Certificate certificate) {
    // Certificates can not be edited, so throw an exception in that case.
    if (certificate.getId() != null) {
      throw new QMutationNotPermittedException(
          "A certificate can not be edited once created.");
    }

    try {
      // Get the issuer CA.
      Ca ca = null;
      if (StringUtils.isNotBlank(certificate.getIssuer())) {
        ca = caService.findByColumn("cn", certificate.getIssuer());
      }

      // Generate a keypair.
      final KeyPair keyPair = keyService.createKeyPair(
          CreateKeyPairRequest.builder()
              .keySize(registryResourceV1.findByName(
                  Registry.SECURITY_ASYMMETRIC_KEY_SIZE).asInt())
              .keyPairGeneratorAlgorithm(
                  registryResourceV1.findByName(
                      Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
              .build()
      );

      // Prepare the sign request.
      CertificateSignRequest certificateSignRequest = new CertificateSignRequest();
      certificateSignRequest
          .setLocale(Locale.US)
          .setPrivateKey(keyPair.getPrivate())
          .setPublicKey(keyPair.getPublic())
          .setSignatureAlgorithm(registryResourceV1.findByName(
              Registry.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM).asString())
          .setSubjectCN(certificate.getCn())
          .setValidForm(Instant.now())
          .setValidTo(certificate.getValidity());

      if (StringUtils.isNotEmpty(certificate.getSan())) {
        certificateSignRequest.setSan(
            Arrays.stream(certificate.getSan().split(",")).map(String::trim)
                .collect(Collectors.joining(","))
        );
      }

      if (ca != null) {
        certificateSignRequest.setIssuerCN(ca.getCn());
        certificateSignRequest.setIssuerPrivateKey(
            keyService.pemToPrivateKey(
                ca.getPrivateKey(),
                registryResourceV1.findByName(
                    Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString()));
      } else {
        certificateSignRequest.setIssuerCN(certificate.getCn());
        certificateSignRequest.setIssuerPrivateKey(keyPair.getPrivate());
      }

      // Sign the certificate.
      final X509CertificateHolder x509CertificateHolder =
          generateCertificate(certificateSignRequest);

      // Populate the certificate DTO to persist it.
      certificate.setIssued(certificateSignRequest.getValidForm());
      certificate
          .setPrivateKey(keyService.privateKeyToPEM(keyPair));
      certificate.setPublicKey(keyService.publicKeyToPEM(keyPair));
      certificate.setIssuer(certificateSignRequest.getIssuerCN());
      certificate
          .setCertificate(certificateToPEM(x509CertificateHolder));
      certificate.setSan(certificateSignRequest.getSan());

      return super.save(certificate);
    } catch (NoSuchAlgorithmException | IOException |
             OperatorCreationException |
             InvalidKeySpecException | NoSuchProviderException e) {
      throw new QCouldNotSaveException("Could not save certificate.", e);
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

}
