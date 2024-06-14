package esthesis.service.crypto.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.crypto.CryptoService;
import esthesis.service.crypto.resource.CryptoInfoResource;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import java.util.List;

@Authenticated
public class CryptoInfoResourceImpl implements CryptoInfoResource {

	@Inject
	CryptoService cryptoService;

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getSupportedKeystoreTypes() {
		return cryptoService.getSupportedKeystoreTypes();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getSupportedKeyAlgorithms() {
		return cryptoService.getSupportedKeyAlgorithms();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getSupportedSignatureAlgorithms() {
		return cryptoService.getSupportedSignatureAlgorithms();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getSupportedMessageDigestAlgorithms() {
		return cryptoService.getSupportedMessageDigestAlgorithms();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getSupportedKeyAgreementAlgorithms() {
		return cryptoService.getSupportedKeyAgreementAlgorithms();
	}
}
