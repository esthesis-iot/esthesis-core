package esthesis.service.crypto.impl.util;

import esthesis.core.common.crypto.CoreCommonCryptoConverters;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

@Slf4j
public class SrvCryptoCryptoConverters extends CoreCommonCryptoConverters {

	private static final String CERTIFICATE = "CERTIFICATE";

	/**
	 * Converts a certificate to a PEM format encoded as X.509.
	 *
	 * @return the generated PEM
	 * @throws IOException thrown when something unexpected happens
	 */
	public static String certificateToPEM(final Certificate certificate)
	throws IOException {
		log.trace("Converting '{}' certificate to PEM.", certificate);
		try (StringWriter pemStrWriter = new StringWriter()) {
			try (PemWriter writer = new PemWriter(pemStrWriter)) {
				writer.writeObject(
					new PemObject(CERTIFICATE, certificate.getEncoded()));
				writer.flush();
				return pemStrWriter.toString();
			}
		}
	}

	public static String privateKeyToPEM(final PrivateKey key) throws IOException {
		try (StringWriter pemStrWriter = new StringWriter()) {
			try (JcaPEMWriter pemWriter = new JcaPEMWriter(pemStrWriter)) {
				pemWriter.writeObject(new JcaPKCS8Generator(key, null));
				pemWriter.flush();
				return pemStrWriter.toString();
			}
		}
	}

	public static String publicKeyToPEM(final PublicKey key) throws IOException {
		try (StringWriter pemStrWriter = new StringWriter()) {
			try (JcaPEMWriter pemWriter = new JcaPEMWriter(pemStrWriter)) {
				pemWriter.writeObject(key);
				pemWriter.flush();
				return pemStrWriter.toString();
			}
		}
	}

	/**
	 * Converts a text-based public key (in PEM format) to {@link PublicKey}.
	 *
	 * @param publicKey the public key in PEM format to convert
	 * @param algorithm the security algorithm with which this key was generated
	 * @return the generated PEM format
	 * @throws NoSuchAlgorithmException thrown when no algorithm is found for encryption
	 * @throws InvalidKeySpecException  thrown when the provided key is invalid
	 */
	public static PublicKey pemToPublicKey(String publicKey, final String algorithm)
	throws NoSuchAlgorithmException, InvalidKeySpecException {
		PublicKey key;

		// Cleanup the PEM from unwanted text.
		publicKey = removeHeaderFooter(publicKey).trim();

		// Read the cleaned up PEM and generate the public key.
		byte[] encoded = Base64.decodeBase64(publicKey);
		final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
		final KeyFactory factory = KeyFactory.getInstance(algorithm);
		key = factory.generatePublic(keySpec);

		return key;
	}

	/**
	 * Converts a {@link KeyStore} to a byte array.
	 *
	 * @param keystore         The keystore to convert.
	 * @param keystorePassword The password of the keystore.
	 */
	public static byte[] keystoreSerialize(final KeyStore keystore,
		final String keystorePassword)
	throws IOException, CertificateException, NoSuchAlgorithmException,
				 KeyStoreException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(baos)) {
			if (StringUtils.isNotBlank(keystorePassword)) {
				keystore.store(bos, keystorePassword.toCharArray());
			} else {
				keystore.store(bos, null);
			}

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
	public static KeyStore keystoreDeserialize(final byte[] keystore,
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
}
