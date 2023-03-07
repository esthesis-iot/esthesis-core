package esthesis.service.crypto.impl.resource;

import esthesis.service.crypto.impl.service.CryptoService;
import esthesis.service.crypto.resource.CryptoInfoResource;
import io.quarkus.security.Authenticated;
import java.util.List;
import javax.inject.Inject;

@Authenticated
public class CryptoInfoResourceImpl implements CryptoInfoResource {

  @Inject
  CryptoService cryptoService;

  @Override
  public List<String> getSupportedKeystoreTypes() {
    return cryptoService.getSupportedKeystoreTypes();
  }

  @Override
  public List<String> getSupportedKeyAlgorithms() {
    return cryptoService.getSupportedKeyAlgorithms();
  }

  @Override
  public List<String> getSupportedSignatureAlgorithms() {
    return cryptoService.getSupportedSignatureAlgorithms();
  }

  @Override
  public List<String> getSupportedMessageDigestAlgorithms() {
    return cryptoService.getSupportedMessageDigestAlgorithms();
  }

  @Override
  public List<String> getSupportedKeyAgreementAlgorithms() {
    return cryptoService.getSupportedKeyAgreementAlgorithms();
  }
}
