package esthesis.service.crypto.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.core.common.dto.SignatureVerificationRequestDTO;
import esthesis.service.crypto.impl.util.CryptoUtil;
import esthesis.service.crypto.resource.SigningSystemResource;
import jakarta.annotation.security.RolesAllowed;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SigningSystemResourceImpl implements SigningSystemResource {

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public boolean verifySignature(SignatureVerificationRequestDTO request)
	throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException,
				 InvalidKeyException {
		return CryptoUtil.verifySignature(request);
	}

}
