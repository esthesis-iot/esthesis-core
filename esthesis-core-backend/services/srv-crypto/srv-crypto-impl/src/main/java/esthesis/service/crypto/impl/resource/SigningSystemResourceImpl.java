package esthesis.service.crypto.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.crypto.CryptoService;
import esthesis.common.crypto.dto.SignatureVerificationRequestDTO;
import esthesis.service.crypto.resource.SigningSystemResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SigningSystemResourceImpl implements SigningSystemResource {

	@Inject
	CryptoService cryptoService;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public boolean verifySignature(SignatureVerificationRequestDTO request)
	throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException,
				 InvalidKeyException {
		return cryptoService.verifySignature(request);
	}

}
