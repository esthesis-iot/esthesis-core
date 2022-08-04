package esthesis.service.crypto.impl.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import esthesis.common.AppConstants.Registry;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.rest.Page;
import esthesis.common.rest.PageReplyFilter;
import esthesis.common.rest.Pageable;
import esthesis.service.crypto.dto.Ca;
import esthesis.service.crypto.dto.CertificateRequest;
import esthesis.service.crypto.dto.ImportCaForm;
import esthesis.service.crypto.dto.KeyPairResponse;
import esthesis.service.crypto.impl.dto.CSR;
import esthesis.service.crypto.impl.dto.CreateKeyPair;
import esthesis.service.crypto.impl.repository.CaRepository;
import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.impl.service.CertificateService;
import esthesis.service.crypto.impl.service.KeyService;
import esthesis.service.crypto.resource.CAResourceV1;
import esthesis.service.registry.dto.RegistryEntry;
import esthesis.service.registry.resource.RegistryResourceV1;
import io.quarkus.security.Authenticated;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

@Authenticated
public class CAResourceV1Impl implements CAResourceV1 {

  @Inject
  JsonWebToken jwt;

  @Inject
  @RestClient
  RegistryResourceV1 registryResourceV1;

  @Inject
  KeyService keyService;

  @Inject
  CAService caService;

  @Inject
  CertificateService certificateService;

  @Inject
  CaRepository caRepository;

  @Inject
  ObjectMapper mapper;

  @Override
  public KeyPairResponse generateKeyPair()
  throws NoSuchAlgorithmException, NoSuchProviderException {
    CreateKeyPair createKeyPairDTO = new CreateKeyPair();
    createKeyPairDTO.setKeySize(
        registryResourceV1.findByName(Registry.SECURITY_ASYMMETRIC_KEY_SIZE)
            .asInt());
    createKeyPairDTO
        .setKeyPairGeneratorAlgorithm(
            registryResourceV1.findByName(
                Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());

    KeyPair keyPair = keyService.createKeyPair(createKeyPairDTO);

    return new KeyPairResponse()
        .setPublicKey(keyPair.getPublic().getEncoded())
        .setPrivateKey(keyPair.getPrivate().getEncoded());
  }

  @Override
  public String publicKeyToPEM(byte[] publicKey) throws IOException {
    return keyService.publicKeyToPEM(publicKey);
  }

  @Override
  public String privateKeyToPEM(byte[] privateKey) throws IOException {
    return keyService.privateKeyToPEM(privateKey);
  }

  @Override
  public String generateCertificateAsPEM(CertificateRequest certificateRequest)
  throws NoSuchAlgorithmException, InvalidKeySpecException,
         OperatorCreationException, IOException {
    // Find the CA defined in the registry.
    RegistryEntry caRegistryEntry = registryResourceV1.findByName(
        Registry.DEVICE_ROOT_CA);
    if (caRegistryEntry == null || StringUtils.isBlank(
        caRegistryEntry.getValue())) {
      throw new QDoesNotExistException(
          "The root CA is not defined in the registry.");
    }
    final Ca ca = caRepository.findById(caRegistryEntry.asObjectId());

    // Generate a certificate sign request.
    PublicKey publicKey = KeyFactory.getInstance(registryResourceV1.findByName(
            Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
        .generatePublic(new X509EncodedKeySpec(
            certificateRequest.getKeyPairResponse().getPublicKey()));
    PrivateKey privateKey = KeyFactory.getInstance(
            registryResourceV1.findByName(
                Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
        .generatePrivate(
            new PKCS8EncodedKeySpec(
                certificateRequest.getKeyPairResponse().getPrivateKey()));
    KeyPair keyPair = new KeyPair(publicKey, privateKey);
    CSR CSRRequest = new CSR()
        .setLocale(Locale.US)
        .setPrivateKey(privateKey)
        .setPublicKey(publicKey)
        .setSignatureAlgorithm(
            registryResourceV1.findByName(
                Registry.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM).asString())
        .setSubjectCN(certificateRequest.getCn())
        .setValidForm(Instant.now())
        .setValidTo(ca.getValidity())
        .setIssuerCN(ca.getCn())
        .setIssuerPrivateKey(
            keyService.pemToPrivateKey(ca.getPrivateKey(),
                registryResourceV1.findByName(
                    Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString()));

    // Sign the certificate.
    X509CertificateHolder x509CertificateHolder = certificateService.generateCertificate(
        CSRRequest);

    // Convert the certificate to PEM and return it.
    return certificateService.certificateToPEM(x509CertificateHolder);
  }

  @GET
  @Override
  @Path("/find")
  @PageReplyFilter(filter = "content,content.id,content.cn,content.issued,content.parentCa,content.parentCaId,content.type,content.validity")
  public Page<Ca> find(@BeanParam Pageable pageable) {
    return caService.find(pageable);
  }

  @GET
  @Override
  @Path("/{id}")
  @PageReplyFilter(filter = "id,cn,issued,parentCa,type,validity,parentCaId")
  public Ca findById(ObjectId id) {
    return caService.findById(id);
  }

  @GET
  @Override
  @Path("/eligible-for-signing")
  @PageReplyFilter(filter = "id,cn")
  public List<Ca> getEligbleForSigning() {
    return caService.getEligibleForSigning();
  }

  @Override
  public Response download(ObjectId caId) {
    try {
      Ca ca = caService.findById(caId);
      String filename = Slugify.builder().underscoreSeparator(true).build()
          .slugify(ca.getCn());
      return ResponseBuilder.ok(mapper.writeValueAsString(findById(caId)))
          .header("Content-Disposition",
              "attachment; filename=" + filename + ".yaml").build()
          .toResponse();
    } catch (JsonProcessingException e) {
      throw new QMismatchException("Could not fetch CA with id '{}'.", caId);
    }
  }

  @Override
  public Ca importCa(ImportCaForm importCaForm) {
    return caService.importCa(importCaForm);
  }

  @Override
  public void delete(ObjectId id) {
    caService.deleteById(id);
  }

  @Override
  public Ca save(Ca ca) {
    return caService.save(ca);
  }
}
