package esthesis.service.crypto.impl.service;

import static esthesis.common.AppConstants.Keystore.Item.KeyType.CERT;
import static esthesis.common.AppConstants.Keystore.Item.KeyType.PRIVATE;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.crypto.CryptoService;
import esthesis.common.exception.QSecurityException;
import esthesis.service.common.BaseService;
import esthesis.service.crypto.dto.KeystoreEntryDTO;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.entity.KeystoreEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
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
	CryptoService cryptoService;

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

	private byte[] addPrivateKeyToKeystore(byte[] keystore, final String keystorePassword,
		final String privateKey, final String keyAlias, final String keyPassword,
		final String certificateChain, final String keystoreType, final String keystoreProvider)
	throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, KeyStoreException,
				 IOException, NoSuchProviderException {
		final PrivateKey pk = cryptoService.pemToPrivateKey(privateKey,
			settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());
		keystore = cryptoService.savePrivateKeyToKeystore(keystore, keystoreType, keystoreProvider,
			keystorePassword, keyAlias + ".key", pk, keyPassword, certificateChain);

		return keystore;
	}

	private byte[] addCertificateToKeystore(byte[] keystore, final String keystorePassword,
		final String certificate, final String certificateAlias, final String keystoreType,
		final String keystoreProvider)
	throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
				 NoSuchProviderException {
		keystore = cryptoService.saveCertificateToKeystore(keystore, keystoreType, keystoreProvider,
			keystorePassword, certificateAlias + ".crt",
			cryptoService.pemToCertificate(certificate).getEncoded());

		return keystore;
	}

	public byte[] download(String keystoreId) {
		try {
			KeystoreEntity keystoreEntity = findById(keystoreId);
			final String keystoreType = keystoreEntity.getType().split("/")[0];
			final String keystoreProvider = keystoreEntity.getType().split("/")[1];
			final String keystorePassword = keystoreEntity.getPassword();

			byte[] keystore = cryptoService.createKeystore(keystoreType, keystoreProvider,
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
}
