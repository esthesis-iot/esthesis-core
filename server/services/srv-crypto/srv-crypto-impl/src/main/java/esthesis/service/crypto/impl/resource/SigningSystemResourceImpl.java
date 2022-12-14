package esthesis.service.crypto.impl.resource;

import esthesis.service.crypto.dto.SignatureVerificationRequestDTO;
import esthesis.service.crypto.impl.service.CryptoService;
import esthesis.service.crypto.resource.SigningSystemResource;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SigningSystemResourceImpl implements SigningSystemResource {

  @Inject
  CryptoService cryptoService;

  @Override
  public boolean verifySignature(SignatureVerificationRequestDTO request)
  throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException,
         InvalidKeyException {
    return cryptoService.verifySignature(request);
  }

}
