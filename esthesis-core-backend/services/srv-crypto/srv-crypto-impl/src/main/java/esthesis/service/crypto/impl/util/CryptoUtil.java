package esthesis.service.crypto.impl.util;

import esthesis.common.exception.QDoesNotExistException;
import esthesis.core.common.dto.SignatureVerificationRequestDTO;
import esthesis.service.crypto.impl.dto.CAHolderDTO;
import esthesis.service.crypto.impl.dto.CertificateSignRequestDTO;
import esthesis.service.crypto.impl.dto.CreateCARequestDTO;
import esthesis.service.crypto.impl.dto.CreateKeyPairRequestDTO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

@Slf4j
public class CryptoUtil {

	private CryptoUtil() {
	}

	private static final Pattern ipv4Pattern = Pattern.compile(
		"^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$");
	private static final String CN = "CN";
	public static final String CERT_TYPE = "X509";

	private static boolean isValidIPV4Address(final String email) {
		Matcher matcher = ipv4Pattern.matcher(email);
		return matcher.matches();
	}

	/**
	 * Returns the default provider for secure random.
	 */
	private static SecureRandom getSecureRandomAlgorithm() throws NoSuchAlgorithmException {
		return getSecureRandomAlgorithm(null);
	}

	/**
	 * Finds the requested secure random algorithm or returns the default one.
	 *
	 * @param secureRandomAlgorithm the secure random algorithm to find.
	 */

	private static SecureRandom getSecureRandomAlgorithm(final String secureRandomAlgorithm)
	throws NoSuchAlgorithmException {
		SecureRandom selectedAlgorithm;
		if (StringUtils.isBlank(secureRandomAlgorithm)) {
			selectedAlgorithm = SecureRandom.getInstanceStrong();
		} else {
			selectedAlgorithm = SecureRandom.getInstance(secureRandomAlgorithm);
		}

		log.debug("Returning secure random algorithm '{}'.", selectedAlgorithm);
		return selectedAlgorithm;
	}

	public static String cleanUpCn(String cn) {
		if (cn.startsWith(CN + "=")) {
			return cn.substring(CN.length() + 1);
		} else {
			return cn;
		}
	}

	/**
	 * Create a new Certificate Authority. This method also supports creating a sub-CA by providing
	 * the issuer's information.
	 *
	 * @param createCARequestDTO the details of the CA to be created
	 * @return the generated certificate
	 * @throws NoSuchAlgorithmException  thrown when no algorithm is found for encryption
	 * @throws InvalidKeySpecException   thrown when the provided key is invalid
	 * @throws OperatorCreationException thrown when something unexpected happens during the
	 *                                   encryption
	 * @throws IOException               thrown when something unexpected happens
	 */
	public static CAHolderDTO createCA(final CreateCARequestDTO createCARequestDTO)
	throws NoSuchAlgorithmException, InvalidKeySpecException, OperatorCreationException, IOException,
				 NoSuchProviderException {
		log.debug("Creating a new CA '{}'.", createCARequestDTO);
		// Create a keypair for this CA.
		final KeyPair keyPair = createKeyPair(createCARequestDTO.getCreateKeyPairRequestDTO());

		// Prepare signing.
		CertificateSignRequestDTO certificateSignRequestDTO = new CertificateSignRequestDTO();
		certificateSignRequestDTO.setValidForm(createCARequestDTO.getValidFrom());
		certificateSignRequestDTO.setValidTo(createCARequestDTO.getValidTo());
		certificateSignRequestDTO.setLocale(createCARequestDTO.getLocale());
		certificateSignRequestDTO.setPublicKey(keyPair.getPublic());
		certificateSignRequestDTO.setPrivateKey(keyPair.getPrivate());
		certificateSignRequestDTO.setSignatureAlgorithm(createCARequestDTO.getSignatureAlgorithm());
		certificateSignRequestDTO.setSubjectCN(createCARequestDTO.getSubjectCN());
		certificateSignRequestDTO.setCa(true);

		// Choose which private key to use. If no parent key is found then this is a self-signed
		// certificate and the private key created for the keypair will be used.
		if (StringUtils.isNotEmpty(createCARequestDTO.getIssuerCN())
			&& createCARequestDTO.getIssuerPrivateKey() != null) {
			certificateSignRequestDTO.setIssuerPrivateKey(createCARequestDTO.getIssuerPrivateKey());
			certificateSignRequestDTO.setIssuerCN(createCARequestDTO.getIssuerCN());
		} else {
			certificateSignRequestDTO.setIssuerPrivateKey(keyPair.getPrivate());
			certificateSignRequestDTO.setIssuerCN(createCARequestDTO.getSubjectCN());
		}

		final X509CertificateHolder certHolder = generateCertificate(certificateSignRequestDTO);

		// Prepare reply.
		final CAHolderDTO cppPemKey = new CAHolderDTO();
		cppPemKey.setPublicKey(keyPair.getPublic());
		cppPemKey.setPrivateKey(keyPair.getPrivate());
		cppPemKey.setCertificate(certHolder.toASN1Structure());

		return cppPemKey;
	}

	/**
	 * Signs a key with another key providing a certificate.
	 *
	 * @param certificateSignRequestDTO the details of the signing to take place
	 * @return the generated signature
	 * @throws OperatorCreationException thrown when something unexpected happens during the
	 *                                   encryption
	 * @throws CertIOException           thrown when something unexpected happens while generating the
	 *                                   certificate
	 */
	@SuppressWarnings({"squid:S2274", "squid:S2142"})
	public static X509CertificateHolder generateCertificate(
		final CertificateSignRequestDTO certificateSignRequestDTO)
	throws OperatorCreationException, CertIOException {
		log.debug("Generating a certificate for '{}'.", certificateSignRequestDTO);
		// Create a generator for the certificate including all certificate details.
		final X509v3CertificateBuilder certGenerator;

		certGenerator = new X509v3CertificateBuilder(new X500Name(
			CN + "=" + StringUtils.defaultIfBlank(certificateSignRequestDTO.getIssuerCN(),
				certificateSignRequestDTO.getSubjectCN())),
			certificateSignRequestDTO.isCa() ? BigInteger.ONE
				: BigInteger.valueOf(Instant.now().toEpochMilli()),
			new Date(certificateSignRequestDTO.getValidForm().toEpochMilli()),
			new Date(certificateSignRequestDTO.getValidTo().toEpochMilli()),
			certificateSignRequestDTO.getLocale(),
			new X500Name(CN + "=" + certificateSignRequestDTO.getSubjectCN()),
			SubjectPublicKeyInfo.getInstance(certificateSignRequestDTO.getPublicKey().getEncoded()));

		// Add SANs.
		if (StringUtils.isNotEmpty(certificateSignRequestDTO.getSan())) {
			GeneralNames subjectAltNames = new GeneralNames(
				Arrays.stream(certificateSignRequestDTO.getSan().split(",")).map(String::trim).map(s -> {
					if (isValidIPV4Address(s)) {
						return new GeneralName(GeneralName.iPAddress, s);
					} else {
						return new GeneralName(GeneralName.dNSName, s);
					}
				}).toArray(GeneralName[]::new));
			certGenerator.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);
		}

		// Check if this is a CA certificate and in that case add the necessary key extensions.
		if (certificateSignRequestDTO.isCa()) {
			certGenerator.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
			certGenerator.addExtension(Extension.keyUsage, true,
				new KeyUsage(KeyUsage.cRLSign | KeyUsage.keyCertSign));
		} else {
			certGenerator.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
		}

		// Generate the certificate.
		final X509CertificateHolder certHolder;
		certHolder = certGenerator.build(
			new JcaContentSignerBuilder(certificateSignRequestDTO.getSignatureAlgorithm()).build(
				certificateSignRequestDTO.getIssuerPrivateKey()));

		return certHolder;
	}

	/**
	 * Generates a new keypair consisting of a public key and a private key.
	 *
	 * @param createKeyPairRequestDTO The details of the keypair to create
	 * @return the generated keypair
	 * @throws NoSuchAlgorithmException thrown when no algorithm is found for encryption
	 */
	public static KeyPair createKeyPair(final CreateKeyPairRequestDTO createKeyPairRequestDTO)
	throws NoSuchAlgorithmException, NoSuchProviderException {
		final KeyPairGenerator keyPairGenerator;
		log.debug("Creating a keypair for '{}'.", createKeyPairRequestDTO);

		// Set the provider.
		if (StringUtils.isNotBlank(createKeyPairRequestDTO.getKeyPairGeneratorAlgorithm())
			&& StringUtils.isNotBlank(createKeyPairRequestDTO.getKeyPairGeneratorProvider())) {
			keyPairGenerator = KeyPairGenerator.getInstance(
				createKeyPairRequestDTO.getKeyPairGeneratorAlgorithm(),
				createKeyPairRequestDTO.getKeyPairGeneratorProvider());
		} else {
			keyPairGenerator = KeyPairGenerator.getInstance(
				createKeyPairRequestDTO.getKeyPairGeneratorAlgorithm());
		}

		// Set the secret provider and generator.
		keyPairGenerator.initialize(createKeyPairRequestDTO.getKeySize(),
			getSecureRandomAlgorithm(createKeyPairRequestDTO.getSecureRandomAlgorithm()));

		return keyPairGenerator.generateKeyPair();
	}

	/**
	 * Converts a byte array holding a private key in DER format to a private key.
	 */
	public static PrivateKey privateKeyFromByteArray(final byte[] key, final String keyAlgorithm,
		final String keyProvider)
	throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
		log.debug("Converting private key '{}' PrivateKey.", key);
		KeyFactory keyFactory;
		if (StringUtils.isNotBlank(keyProvider)) {
			keyFactory = KeyFactory.getInstance(keyAlgorithm, keyProvider);
		} else {
			keyFactory = KeyFactory.getInstance(keyAlgorithm);
		}
		EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(key);
		return keyFactory.generatePrivate(encodedKeySpec);
	}

	/**
	 * Creates an empty keystore. This keystore can later on be used to add keys and certificates into
	 * it.
	 *
	 * @param keystoreType     The type of the keystore to create.
	 * @param keystoreProvider The provider for the specific keystore type.
	 * @param keystorePassword The password of the keystore.
	 */
	public static byte[] createKeystore(final String keystoreType, final String keystoreProvider,
		final String keystorePassword)
	throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, IOException,
				 CertificateException {
		// Create a new keystore.
		KeyStore ks;
		if (StringUtils.isBlank(keystoreType) || StringUtils.isBlank(keystoreProvider)) {
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

		return CryptoConvertersUtil.keystoreSerialize(ks, keystorePassword);
	}

	/**
	 * Saves a certificate to the keystore. If the certificate identified by the alias already exists
	 * it gets overwritten.
	 *
	 * @param keystore         The keystore to save the symmetric key into.
	 * @param keystoreType     The type of the keystore.
	 * @param keystoreProvider The provider for the specific type of keystore.
	 * @param keystorePassword The password of the keystore.
	 * @param certificate      The certificate to save.
	 * @param certificateAlias The alias under which the certificate is saved.
	 */
	public static byte[] saveCertificateToKeystore(final byte[] keystore, final String keystoreType,
		final String keystoreProvider, final String keystorePassword, final String certificateAlias,
		final byte[] certificate)
	throws NoSuchAlgorithmException, CertificateException, NoSuchProviderException, KeyStoreException,
				 IOException {
		// Load the keystore.
		KeyStore ks = CryptoConvertersUtil.keystoreDeserialize(keystore, keystoreType,
			keystorePassword,
			keystoreProvider);

		// Add the certificate.
		ks.setCertificateEntry(certificateAlias,
			new JcaX509CertificateConverter().getCertificate(new X509CertificateHolder(certificate)));

		return CryptoConvertersUtil.keystoreSerialize(ks, keystorePassword);
	}

	/**
	 * Saves a private key to the keystore. If the key identified by the alias of the key already
	 * exists, it gets overwritten.
	 *
	 * @param keystore         The keystore to save the symmetric key into.
	 * @param keystoreType     The type of the keystore.
	 * @param keystoreProvider The provider for the specific type of keystore.
	 * @param keystorePassword The password of the keystore.
	 * @param keyAlias         The alias under which the key will be saved.
	 * @param key              The key to save in DER format.
	 * @param keyPassword      The password of the key.
	 * @param certificateChain The certificate chain for the key.
	 */
	@SuppressWarnings("squid:S00107")
	public static byte[] savePrivateKeyToKeystore(final byte[] keystore, final String keystoreType,
		final String keystoreProvider, final String keystorePassword, final String keyAlias,
		final PrivateKey key, final String keyPassword, final String certificateChain)
	throws NoSuchAlgorithmException, CertificateException, NoSuchProviderException, KeyStoreException,
				 IOException {
		// Load the keystore.
		KeyStore ks = CryptoConvertersUtil.keystoreDeserialize(keystore, keystoreType,
			keystorePassword,
			keystoreProvider);

		Collection<? extends Certificate> certificates = CertificateFactory.getInstance("X.509")
			.generateCertificates(
				new ByteArrayInputStream(certificateChain.getBytes(StandardCharsets.UTF_8)));

		// Add the key.
		ks.setKeyEntry(keyAlias, key,
			keyPassword != null ? keyPassword.toCharArray() : "".toCharArray(),
			certificates.toArray(new Certificate[0]));

		return CryptoConvertersUtil.keystoreSerialize(ks, keystorePassword);
	}

	/**
	 * Verifies a signature.
	 *
	 * @param request the details of the signature to verify
	 * @return true if the signature is verified, false if it is not
	 * @throws NoSuchAlgorithmException thrown when no algorithm is found for encryption
	 * @throws InvalidKeySpecException  thrown when the provided key is invalid
	 * @throws InvalidKeyException      thrown when the provided key is invalid
	 * @throws SignatureException       thrown when something unexpected occurs during signing
	 */
	public static boolean verifySignature(final SignatureVerificationRequestDTO request)
	throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
				 SignatureException {
		log.debug("Received signature verification request '{}'.", request);
		if (StringUtils.isBlank(request.getSignature())) {
			throw new QDoesNotExistException("The provided signature to validate is empty.");
		}
		final Signature signature = Signature.getInstance(request.getSignatureAlgorithm());
		signature.initVerify(CryptoConvertersUtil.pemToPublicKey(request.getPublicKey(),
			request.getKeyAlgorithm()));
		signature.update(request.getPayload());

		boolean verification = signature.verify(Base64.getDecoder().decode(request.getSignature()));
		log.debug("Signature verification result is '{}'.", verification);

		return verification;
	}

	/**
	 * Returns a list of all keystore types supported in the underlying JVM.
	 *
	 * @return
	 */
	public static List<String> getSupportedKeystoreTypes() {
		List<String> types = new ArrayList<>();
		Provider[] providers = Security.getProviders();
		for (Provider provider : providers) {
			for (Provider.Service service : provider.getServices()) {
				if ("KeyStore".equals(service.getType())) {
					types.add(service.getAlgorithm() + "/" + provider.getName());
				}
			}
		}
		return types;
	}

	/**
	 * Returns a list of all key algorithms supported in the underlying JVM.
	 *
	 * @return
	 */
	public static List<String> getSupportedKeyAlgorithms() {
		List<String> algorithms = new ArrayList<>();
		Provider[] providers = Security.getProviders();
		for (Provider provider : providers) {
			for (Provider.Service service : provider.getServices()) {
				if ("KeyFactory".equals(service.getType())) {
					algorithms.add(service.getAlgorithm() + "/" + provider.getName());
				}
			}
		}
		return algorithms;
	}

	/**
	 * Returns a list of all signature algorithms supported in the underlying JVM.
	 *
	 * @return
	 */
	public static List<String> getSupportedSignatureAlgorithms() {
		List<String> algorithms = new ArrayList<>();
		Provider[] providers = Security.getProviders();
		for (Provider provider : providers) {
			for (Provider.Service service : provider.getServices()) {
				if ("Signature".equals(service.getType())) {
					algorithms.add(service.getAlgorithm() + "/" + provider.getName());
				}
			}
		}
		return algorithms;
	}

	/**
	 * Returns a list of all message digest algorithms supported in the underlying JVM.
	 *
	 * @return
	 */
	public static List<String> getSupportedMessageDigestAlgorithms() {
		List<String> algorithms = new ArrayList<>();
		Provider[] providers = Security.getProviders();
		for (Provider provider : providers) {
			for (Provider.Service service : provider.getServices()) {
				if ("MessageDigest".equals(service.getType())) {
					algorithms.add(service.getAlgorithm() + "/" + provider.getName());
				}
			}
		}
		return algorithms;
	}

	/**
	 * Returns a list of all key agreement algorithms supported in the underlying JVM.
	 *
	 * @return
	 */
	public static List<String> getSupportedKeyAgreementAlgorithms() {
		List<String> algorithms = new ArrayList<>();
		Provider[] providers = Security.getProviders();
		for (Provider provider : providers) {
			for (Provider.Service service : provider.getServices()) {
				if ("KeyAgreement".equals(service.getType())) {
					algorithms.add(service.getAlgorithm() + "/" + provider.getName());
				}
			}
		}
		return algorithms;
	}
}
