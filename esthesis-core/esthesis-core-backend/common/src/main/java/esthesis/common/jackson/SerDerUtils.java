package esthesis.common.jackson;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.SneakyThrows;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class SerDerUtils {

  private SerDerUtils() {
  }

  /**
   * @param publicKey DER Base64 encoded public key.
   */
  @SneakyThrows
  public static PublicKey getPublicKey(String publicKey) {
    final byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
    final String oid = SubjectPublicKeyInfo.getInstance(publicKeyBytes)
        .getAlgorithm().getAlgorithm().toString();

    return KeyFactory.getInstance(oid, new org.bouncycastle.jce.provider.BouncyCastleProvider())
        .generatePublic(new X509EncodedKeySpec(publicKeyBytes));
  }

  /**
   * @param privateKey DER Base64 encoded private key.
   */
  public static PrivateKey getPrivateKey(String privateKey) throws PEMException {
    byte[] privateKeyBytes = Base64.getDecoder().decode(privateKey);
    final ASN1Sequence asn1 = ASN1Sequence.getInstance(privateKeyBytes);
    final PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(asn1);
    final JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

    return converter.getPrivateKey(privateKeyInfo);
  }
}
