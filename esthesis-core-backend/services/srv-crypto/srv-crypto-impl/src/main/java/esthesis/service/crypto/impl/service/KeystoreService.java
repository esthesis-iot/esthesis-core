package esthesis.service.crypto.impl.service;

import static esthesis.core.common.AppConstants.Keystore.Item.KeyType.CERT;
import static esthesis.core.common.AppConstants.Keystore.Item.KeyType.PRIVATE;
import static esthesis.core.common.AppConstants.ROLE_SYSTEM;
import static esthesis.core.common.AppConstants.Security.Category.CRYPTO;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.common.crypto.CryptoConvertersUtil;
import esthesis.common.crypto.CryptoUtil;
import esthesis.common.exception.QSecurityException;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.dto.KeystoreEntryDTO;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.entity.KeystoreEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.settings.resource.SettingsResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Service for handling keystore entities.
 */
@Transactional
@ApplicationScoped
public class KeystoreService extends BaseService<KeystoreEntity> {

	@Inject
	CertificateService certificateService;

	@Inject
	CAService caService;

	@Inject
	@RestClient
	DeviceResource deviceResource;

	@Inject
	@RestClient
	SettingsResource settingsResource;

	/**
	 * Adds a private key to a keystore.
	 *
	 * @param keystore         the keystore to add the key to.
	 * @param keystorePassword the password of the keystore.
	 * @param privateKey       the private key to add.
	 * @param keyAlias         the alias of the key.
	 * @param keyPassword      the password of the key.
	 * @param certificateChain the certificate chain of the key.
	 * @param keystoreType     the type of the keystore.
	 * @param keystoreProvider the provider of the keystore.
	 * @return the keystore with the added key.
	 * @throws NoSuchAlgorithmException if the algorithm is not found.
	 * @throws InvalidKeySpecException  if the key spec is invalid.
	 * @throws CertificateException     if the certificate is invalid.
	 * @throws KeyStoreException        if the keystore is invalid.
	 * @throws IOException              if an I/O error occurs.
	 * @throws NoSuchProviderException  if the provider is not found.
	 */
	@SuppressWarnings("java:S107")
	private byte[] addPrivateKeyToKeystore(byte[] keystore, final String keystorePassword,
		final String privateKey, final String keyAlias, final String keyPassword,
		final String certificateChain, final String keystoreType, final String keystoreProvider)
	throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, KeyStoreException,
				 IOException, NoSuchProviderException {
		final PrivateKey pk = CryptoConvertersUtil.pemToPrivateKey(privateKey,
			settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());
		keystore = CryptoUtil.savePrivateKeyToKeystore(keystore, keystoreType,
			keystoreProvider,
			keystorePassword, keyAlias + ".key", pk, keyPassword, certificateChain);

		return keystore;
	}

	/**
	 * Adds a certificate to a keystore.
	 *
	 * @param keystore         the keystore to add the certificate to.
	 * @param keystorePassword the password of the keystore.
	 * @param certificate      the certificate to add.
	 * @param certificateAlias the alias of the certificate.
	 * @param keystoreType     the type of the keystore.
	 * @param keystoreProvider the provider of the keystore.
	 * @return the keystore with the added certificate.
	 * @throws CertificateException     if the certificate is invalid.
	 * @throws NoSuchAlgorithmException if the algorithm is not found.
	 * @throws KeyStoreException        if the keystore is invalid.
	 * @throws IOException              if an I/O error occurs.
	 * @throws NoSuchProviderException  if the provider is not found.
	 */
	private byte[] addCertificateToKeystore(byte[] keystore, final String keystorePassword,
		final String certificate, final String certificateAlias, final String keystoreType,
		final String keystoreProvider)
	throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
				 NoSuchProviderException {
		keystore = CryptoUtil.saveCertificateToKeystore(keystore, keystoreType,
			keystoreProvider,
			keystorePassword, certificateAlias + ".crt",
			CryptoConvertersUtil.pemToCertificate(certificate).getEncoded());

		return keystore;
	}

	private KeystoreEntity saveHandler(KeystoreEntity entity) {
		return super.save(entity);
	}

	/**
	 * Downloads a keystore.
	 *
	 * @param keystoreId the id of the keystore to download.
	 * @return the keystore.
	 */
	@SuppressWarnings("java:S3776")
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public byte[] download(String keystoreId) {
		try {
			KeystoreEntity keystoreEntity = findById(keystoreId);
			final String keystoreType = keystoreEntity.getType().split("/")[0];
			final String keystoreProvider = keystoreEntity.getType().split("/")[1];
			final String keystorePassword = keystoreEntity.getPassword();

			byte[] keystore = CryptoUtil.createKeystore(keystoreType, keystoreProvider,
				keystorePassword);

			for (KeystoreEntryDTO entry : keystoreEntity.getEntries()) {
				switch (entry.getResourceType()) {
					case DEVICE:
						DeviceEntity deviceEntity = deviceResource.get(entry.getId());
						if (entry.getKeyType().contains(PRIVATE)) {
							keystore = addPrivateKeyToKeystore(keystore, keystorePassword,
								deviceEntity.getDeviceKey().getPrivateKey(), deviceEntity.getHardwareId(),
								entry.getPassword(), deviceEntity.getDeviceKey().getCertificate(), keystoreType,
								keystoreProvider);
						}
						if (entry.getKeyType().contains(CERT)) {
							keystore = addCertificateToKeystore(keystore, keystorePassword,
								deviceEntity.getDeviceKey().getCertificate(), deviceEntity.getHardwareId(),
								keystoreType, keystoreProvider);
						}
						break;
					case CERT:
						CertificateEntity certificateEntity = certificateService.findById(entry.getId());
						if (entry.getKeyType().contains(PRIVATE)) {
							keystore = addPrivateKeyToKeystore(keystore, keystorePassword,
								certificateEntity.getPrivateKey(), certificateEntity.getName(),
								entry.getPassword(), certificateEntity.getCertificate(), keystoreType,
								keystoreProvider);
						}
						if (entry.getKeyType().contains(CERT)) {
							keystore = addCertificateToKeystore(keystore, keystorePassword,
								certificateEntity.getCertificate(), certificateEntity.getName(),
								keystoreType, keystoreProvider);
						}
						break;
					case CA:
						CaEntity caEntity = caService.findById(entry.getId());
						if (entry.getKeyType().contains(PRIVATE)) {
							keystore = addPrivateKeyToKeystore(keystore, keystorePassword,
								caEntity.getPrivateKey(), caEntity.getName(),
								entry.getPassword(), caEntity.getCertificate(), keystoreType,
								keystoreProvider);
						}
						if (entry.getKeyType().contains(CERT)) {
							keystore = addCertificateToKeystore(keystore, keystorePassword,
								caEntity.getCertificate(), caEntity.getName(),
								keystoreType, keystoreProvider);
						}
						break;
					case TAG:
						for (DeviceEntity entity : deviceResource.findByTagName(entry.getName())) {
							if (entry.getKeyType().contains(PRIVATE)) {
								keystore = addPrivateKeyToKeystore(keystore, keystorePassword,
									entity.getDeviceKey().getPrivateKey(), entity.getHardwareId(),
									entry.getPassword(), entity.getDeviceKey().getCertificate(), keystoreType,
									keystoreProvider);
							}
							if (entry.getKeyType().contains(CERT)) {
								keystore = addCertificateToKeystore(keystore, keystorePassword,
									entity.getDeviceKey().getCertificate(), entity.getHardwareId(),
									keystoreType, keystoreProvider);
							}
						}
						break;
				}
			}

			return keystore;
		} catch (Exception e) {
			throw new QSecurityException("Error while creating keystore", e);
		}
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public KeystoreEntity findById(String id) {
		return super.findById(id);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public Page<KeystoreEntity> find(Pageable pageable) {
		return super.find(pageable);
	}

	/**
	 * Saves a new keystore.
	 *
	 * @param entity the keystore to save.
	 * @return the saved keystore.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = CREATE)
	public KeystoreEntity saveNew(KeystoreEntity entity) {
		return saveHandler(entity);
	}

	/**
	 * Updates a keystore.
	 *
	 * @param entity the keystore to update.
	 * @return the updated keystore.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = WRITE)
	public KeystoreEntity saveUpdate(KeystoreEntity entity) {
		return saveHandler(entity);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = DELETE)
	public boolean deleteById(String deviceId) {
		return super.deleteById(deviceId);
	}
}
