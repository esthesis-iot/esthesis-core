package esthesis.service.crypto.impl.service;

import static esthesis.core.common.AppConstants.Keystore.Item.KeyType.CERT;
import static esthesis.core.common.AppConstants.Keystore.Item.KeyType.PRIVATE;
import static esthesis.core.common.AppConstants.ROLE_SYSTEM;
import static esthesis.core.common.AppConstants.Security.Category.CRYPTO;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.common.exception.QSecurityException;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.dto.KeystoreEntryDTO;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.entity.KeystoreEntity;
import esthesis.service.crypto.impl.util.CryptoConvertersUtil;
import esthesis.service.crypto.impl.util.CryptoUtil;
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
	public Page<KeystoreEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, true);
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = CREATE)
	public KeystoreEntity saveNew(KeystoreEntity entity) {
		return saveHandler(entity);
	}

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
