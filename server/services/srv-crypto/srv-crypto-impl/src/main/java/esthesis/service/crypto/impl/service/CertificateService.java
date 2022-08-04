package esthesis.service.crypto.impl.service;

import esthesis.service.crypto.impl.dto.CSR;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
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

@Slf4j
@ApplicationScoped
public class CertificateService {

  private static final String CN = "CN";
  private static final String CERTIFICATE = "CERTIFICATE";
  private final String IPV4_PATTERN = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
  private final Pattern ipv4Pattern = Pattern.compile(IPV4_PATTERN);

  private boolean isValidIPV4Address(final String email) {
    Matcher matcher = ipv4Pattern.matcher(email);
    return matcher.matches();
  }

  /**
   * Signs a key with another key providing a certificate.
   *
   * @param CSR the details of the signing to take place
   * @return the generated signature
   * @throws OperatorCreationException thrown when something unexpected happens
   *                                   during the encryption
   * @throws CertIOException           thrown when something unexpected happens
   *                                   while generating the certificate
   */
  @SuppressWarnings({"squid:S2274", "squid:S2142"})
  public X509CertificateHolder generateCertificate(final CSR CSR)
  throws OperatorCreationException, CertIOException {
    log.trace("Generating a certificate for '{}'.", CSR);
    // Create a generator for the certificate including all certificate details.
    final X509v3CertificateBuilder certGenerator;
    // Synchronize this part, so that no two certificates can be created with the same timestamp.

    synchronized (this) {
      certGenerator = new X509v3CertificateBuilder(new X500Name(
          CN + "=" + StringUtils.defaultIfBlank(
              CSR.getIssuerCN(),
              CSR.getSubjectCN())),
          CSR.isCa() ? BigInteger.ONE
              : BigInteger.valueOf(Instant.now().toEpochMilli()),
          new Date(CSR.getValidForm().toEpochMilli()),
          new Date(CSR.getValidTo().toEpochMilli()),
          CSR.getLocale(),
          new X500Name(CN + "=" + CSR.getSubjectCN()),
          SubjectPublicKeyInfo.getInstance(
              CSR.getPublicKey().getEncoded()));
    }

    // Add SANs.
    if (StringUtils.isNotEmpty(CSR.getSan())) {
      GeneralNames subjectAltNames = new GeneralNames(
          Arrays.stream(CSR.getSan().split(","))
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
    if (CSR.isCa()) {
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
            CSR.getSignatureAlgorithm()).build(
            CSR.getIssuerPrivateKey()));

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
    log.debug("Parsing '{}' PEM certificate.", cert);
    CertificateFactory fact = CertificateFactory.getInstance("X.509");

    return (X509Certificate) fact.generateCertificate(
        new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
  }
}
