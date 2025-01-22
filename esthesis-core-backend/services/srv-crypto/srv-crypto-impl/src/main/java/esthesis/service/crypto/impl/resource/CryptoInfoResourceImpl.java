package esthesis.service.crypto.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.crypto.impl.util.CryptoUtil;
import esthesis.service.crypto.resource.CryptoInfoResource;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;

@Authenticated
public class CryptoInfoResourceImpl implements CryptoInfoResource {

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getSupportedKeystoreTypes() {
		return CryptoUtil.getSupportedKeystoreTypes();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getSupportedKeyAlgorithms() {
		return CryptoUtil.getSupportedKeyAlgorithms();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getSupportedSignatureAlgorithms() {
		return CryptoUtil.getSupportedSignatureAlgorithms();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getSupportedMessageDigestAlgorithms() {
		return CryptoUtil.getSupportedMessageDigestAlgorithms();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getSupportedKeyAgreementAlgorithms() {
		return CryptoUtil.getSupportedKeyAgreementAlgorithms();
	}
}
