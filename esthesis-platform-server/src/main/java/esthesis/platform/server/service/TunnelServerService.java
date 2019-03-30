package esthesis.platform.server.service;

import com.eurodyn.qlack.fuse.crypto.CryptoConversionService;
import com.eurodyn.qlack.fuse.crypto.CryptoKeyService;
import com.eurodyn.qlack.fuse.crypto.CryptoSignService;
import com.eurodyn.qlack.util.data.encryption.Encrypt;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.TunnelServerDTO;
import esthesis.platform.server.mapper.TunnelServerMapper;
import esthesis.platform.server.model.TunnelServer;
import esthesis.platform.server.repository.TunnelServerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;

@Service
@Transactional
@Validated
public class TunnelServerService extends BaseService<TunnelServerDTO, TunnelServer> {

  private final CryptoKeyService cryptoKeyService;
  private final CryptoSignService cryptoSignService;
  private final CAService caService;
  private final AppProperties appProperties;
  private final CryptoConversionService cryptoConversionService;
  private final SecurityService securityService;
  private final ObjectMapper objectMapper;
  private final TunnelServerMapper tunnelServerMapper;
  private final TunnelServerRepository tunnelServerRepository;

  public TunnelServerService(CryptoKeyService cryptoKeyService,
      CryptoSignService cryptoSignService, CAService caService,
      AppProperties appProperties, CryptoConversionService cryptoConversionService,
      SecurityService securityService, ObjectMapper objectMapper,
      TunnelServerMapper tunnelServerMapper,
      TunnelServerRepository tunnelServerRepository) {
    this.cryptoKeyService = cryptoKeyService;
    this.cryptoSignService = cryptoSignService;
    this.caService = caService;
    this.appProperties = appProperties;
    this.cryptoConversionService = cryptoConversionService;
    this.securityService = securityService;
    this.objectMapper = objectMapper;
    this.tunnelServerMapper = tunnelServerMapper;
    this.tunnelServerRepository = tunnelServerRepository;
  }

  @Override
  public TunnelServerDTO save(TunnelServerDTO tunnelServerDTO) {
//    if (tunnelServerDTO.getId() != null) {
//      try {
//        tunnelServerDTO.setStatus(true);
//
//        // Generate keys.
//        final KeyPair keyPair;
//        keyPair = cryptoKeyService
//            .createKeyPair(CreateKeyPairDTO.builder().keySize(appProperties.getSecurityCaKeypairKeySize())
//                .generatorAlgorithm(appProperties.getSecurityCaKeypairGeneratorAlgorithm())
//                .generatorProvider(appProperties.getSecurityCaKeypairGeneratorProvider())
//                .secretAlgorithm(appProperties.getSecurityCaKeypairSecrectAlgorithm())
//                .secretProvider(appProperties.getSecurityCaKeypairSecrectProvider())
//                .build());
//
//        // Find parent CA.
//        final CaDTO parentCaDTO = caService.findById(Long.parseLong(tunnelServerDTO.getParentCa()));
//
//        // Get the private key of the parent CA.
//        final PrivateKey privateKey = cryptoConversionService
//            .pemToPrivateKey(securityService.decrypt(parentCaDTO.getPrivateKey()),
//                appProperties.getSecurityCaKeypairGeneratorProvider(),
//                appProperties.getSecurityCaKeypairGeneratorAlgorithm());
//
//        // Sign.
//        final X509CertificateHolder certHolder = cryptoSignService.signKey(
//            SignDTO.builder()
//                .validForm(Instant.now())
//                .validto(Instant.now().plus(365, ChronoUnit.DAYS))
//                .issuerCN(parentCaDTO.getCn())
//                .locale(Locale.US)
//                .privateKey(privateKey)
//                .publicKey(keyPair.getPublic())
//                .signatureAlgorithm(appProperties.getSecurityCaKeypairSignatureAlgorithm())
//                .signatureProvider(appProperties.getSecurityCaKeypairSignatureProvider())
//                .subjectCN(tunnelServerDTO.getName())
//                .build());
//
//        tunnelServerDTO.setPublicKey(cryptoConversionService.publicKeyToPEM(keyPair));
//        tunnelServerDTO.setPrivateKey(securityService.encrypt(cryptoConversionService.privateKeyToPEM(keyPair)));
//        tunnelServerDTO.setCertificate(cryptoConversionService.certificateToPEM(certHolder));
//      } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidKeySpecException | OperatorCreationException
//          | IOException e) {
//        throw new QCouldNotSaveException("Could not save Tunnel server.", e);
//      }
//    } else {
//      final TunnelServer tsLookup = ReturnOptional
//          .r(tunnelServerRepository.findById(tunnelServerDTO.getId()));
//      tunnelServerDTO.setPublicKey(tsLookup.getPublicKey());
//      tunnelServerDTO.setPrivateKey(tsLookup.getPrivateKey());
//      tunnelServerDTO.setCertificate(tsLookup.getCertificate());
//    }
//
//    return tunnelServerMapper.map(tunnelServerRepository.save(tunnelServerMapper.map(tunnelServerDTO)));
    return null;
  }

  public String backup(long tunnelServerId) throws IOException {
    final TunnelServer tunnelServer = ReturnOptional.r(tunnelServerRepository.findById(tunnelServerId));
    tunnelServer.setPrivateKey(securityService.decrypt(tunnelServer.getPrivateKey()));
    tunnelServer.setToken(securityService.decrypt(tunnelServer.getToken()));

    return objectMapper.writeValueAsString(tunnelServer);
  }

  public void restore(String backup) throws IOException {
    // Create a local copy of the system's ObjectMapper in order to overwrite Access.READ_ONLY attributes of the
    // underlying object.
    ObjectMapper localObjectMapper = objectMapper.copy();
    localObjectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
    final TunnelServerDTO tunnelServerDTO = localObjectMapper.readValue(backup, TunnelServerDTO.class);
    tunnelServerDTO.setToken(securityService.encrypt(tunnelServerDTO.getToken()));
    tunnelServerDTO.setPrivateKey(securityService.encrypt(tunnelServerDTO.getPrivateKey()));

    save(tunnelServerDTO);
  }

}
