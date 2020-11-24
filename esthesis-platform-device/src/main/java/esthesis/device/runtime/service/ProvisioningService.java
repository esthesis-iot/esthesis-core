package esthesis.device.runtime.service;

import com.eurodyn.qlack.common.exception.QSecurityException;
import com.eurodyn.qlack.fuse.crypto.service.CryptoDigestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.device.runtime.config.AppConstants;
import esthesis.device.runtime.config.AppProperties;
import esthesis.device.runtime.model.Provisioning;
import esthesis.device.runtime.repository.ProvisioningRepository;
import esthesis.device.runtime.util.DeviceMessageUtil;
import esthesis.platform.backend.common.device.ProvisioningInfoRequest;
import esthesis.platform.backend.common.device.ProvisioningInfoResponse;
import esthesis.platform.backend.common.device.ProvisioningRequest;
import esthesis.platform.backend.common.dto.DeviceMessage;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.java.Log;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;

@Log
@Service
@Validated
public class ProvisioningService {

  private final AppProperties appProperties;
  private final RegistrationService registrationService;
  private final DeviceMessageUtil deviceMessageUtil;
  private final RetryTemplate retryTemplate;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final CryptoDigestService cryptoDigestService;
  private final ProvisioningRepository provisioningRepository;

  @SuppressWarnings("java:S107")
  public ProvisioningService(AppProperties appProperties,
    RegistrationService registrationService,
    DeviceMessageUtil deviceMessageUtil,
    RetryTemplate retryTemplate, RestTemplate restTemplate,
    ObjectMapper objectMapper,
    CryptoDigestService cryptoDigestService,
    ProvisioningRepository provisioningRepository) {
    this.appProperties = appProperties;
    this.registrationService = registrationService;
    this.deviceMessageUtil = deviceMessageUtil;
    this.retryTemplate = retryTemplate;
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
    this.cryptoDigestService = cryptoDigestService;
    this.provisioningRepository = provisioningRepository;
  }

  public boolean isInitialProvisioningDone() {
    return appProperties.isSkipInitialProvisioning()
      || provisioningRepository.countAllByIsInitialProvisioning(true) > 0;
  }

  /**
   * Downloads a provisioning package.
   *
   * @return Returns the ID of the provisioning package that was downloaded or null if no package
   * was downloaded.
   */
  public Long provisioning()
  throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
         SignatureException, InvalidAlgorithmParameterException, NoSuchPaddingException {
    // Do not attempt provisioning if a provisioning URL was not provided.
    if (StringUtils.isEmpty(registrationService.getProvisioningUrl())) {
      log.log(Level.WARNING, "Requested " + (isInitialProvisioningDone() ? "initial " : "") +
        "provisioning, however a provisioning URL has not been provided for this device");
      return null;
    }

    // Proceed with provisioning.
    String provisioningUrl =
      registrationService.getProvisioningUrl() + AppConstants.URL_PS_PROVISIONING;
    log.log(Level.CONFIG, "Attempting " + (isInitialProvisioningDone() ? "initial " : "") +
      "provisioning at {0}.", provisioningUrl);
    String provisioningInfoUrl =
      registrationService.getProvisioningUrl() + AppConstants.URL_PS_PROVISIONING_INFO;
    String provisioningDownloadUrl =
      registrationService.getProvisioningUrl() + AppConstants.URL_PS_PROVISIONING_DOWNLOAD;

    // Get the details of the package to be downloaded.
    DeviceMessage<ProvisioningInfoRequest> provisioningInfoRequest = new DeviceMessage<>(
      appProperties.getHardwareId());
    deviceMessageUtil.prepareOutgoing(provisioningInfoRequest);

    // Fire up request.
    //noinspection unchecked
    DeviceMessage<ProvisioningInfoResponse> provisioningInfoResponse =
      retryTemplate.execute(context ->
        restTemplate
          .exchange(provisioningInfoUrl, HttpMethod.POST, new HttpEntity(provisioningInfoRequest),
            new ParameterizedTypeReference<DeviceMessage<ProvisioningInfoResponse>>() {
            }).getBody());

    // Verify reply.
    deviceMessageUtil.processIncoming(provisioningInfoResponse, ProvisioningInfoResponse.class);

    // Check if the reply contains a provisioning package that should be downloaded.
    if (provisioningInfoResponse.getPayload() == null) {
      log.log(Level.FINE, "No provisioning package found to download.");
      return null;
    }

    // Check that the provisioning package suggested is not already downloaded.
    final Provisioning existingProvisioning = provisioningRepository
      .findByPackageId(provisioningInfoResponse.getPayload().getId());
    if (existingProvisioning != null) {
      log.log(Level.FINE, "Ignored provisioning package with ID ''{0}'', file ''{1}'', "
        + "version ''{2}'' as it was already downloaded on ''{3}''.", new Object[]{
        provisioningInfoResponse.getPayload().getId(),
        provisioningInfoResponse.getPayload().getFileName(),
        provisioningInfoResponse.getPayload().getPackageVersion(),
        existingProvisioning.getProvisionedOn()});
      return null;
    }

    // Proceed downloading the provisioning package.
    Path tmpDownloadFile = Paths.get(appProperties.getProvisioningTempRoot(),
      provisioningInfoResponse.getPayload().getFileName());
    Files.deleteIfExists(tmpDownloadFile);

    log.log(Level.FINE, "Downloading {0} ({1}) to {2}.",
      new Object[]{provisioningInfoResponse.getPayload().getFileName(),
        FileUtils.byteCountToDisplaySize(provisioningInfoResponse.getPayload().getFileSize()),
        tmpDownloadFile});

    DeviceMessage<ProvisioningRequest> provisioningRequest = new DeviceMessage<>(
      appProperties.getHardwareId());
    deviceMessageUtil.prepareOutgoing(provisioningRequest);

    RequestCallback requestCallback = request -> {
      request.getHeaders()
        .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
      request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
      request.getBody().write(objectMapper.writeValueAsBytes(provisioningRequest));
    };
    ResponseExtractor<Void> responseExtractor = response -> {
      Files.copy(response.getBody(), tmpDownloadFile);
      return null;
    };
    restTemplate.execute(
      URI.create(provisioningDownloadUrl + "/" + provisioningInfoResponse.getPayload().getId()),
      HttpMethod.POST, requestCallback, responseExtractor);

    // Verify signature if requested so.
    if (appProperties.isProvisioningSigned()) {
      deviceMessageUtil.verifySignature(tmpDownloadFile,
        provisioningInfoResponse.getPayload().getSignature());
    }

    // Decrypt if encrypted is requested so.
    if (appProperties.isProvisioningEncrypted()) {
      String decryptedFile = deviceMessageUtil.decrypt(tmpDownloadFile, true);
      Files.move(Paths.get(decryptedFile), tmpDownloadFile);
    }

    // Validate SHA256.
    try (FileInputStream fileInputStream = new FileInputStream(
      tmpDownloadFile.toFile().getAbsoluteFile())) {
      if (!provisioningInfoResponse.getPayload().getSha256()
        .equals(cryptoDigestService.sha256(fileInputStream))) {
        throw new QSecurityException("SHA256 for provisioning package does not match.");
      }
    }

    // Rename the temporary file downloaded.
    Path finalFile = Paths.get(appProperties.getProvisioningRoot(),
      provisioningInfoResponse.getPayload().getFileName());
    Files.deleteIfExists(finalFile);
    Files.move(tmpDownloadFile, finalFile);

    // Record this provisioning package download action.
    Provisioning provisioning = new Provisioning();
    provisioning.setInitialProvisioning(!isInitialProvisioningDone());
    provisioning.setName(provisioningInfoResponse.getPayload().getName());
    provisioning.setVersion(provisioningInfoResponse.getPayload().getPackageVersion());
    provisioning.setFilename(provisioningInfoResponse.getPayload().getFileName());
    provisioning.setPackageId(provisioningInfoResponse.getPayload().getId());
    provisioning.setSha256(provisioningInfoResponse.getPayload().getSha256());
    provisioning.setProvisionedOn(new Date());
    provisioningRepository.save(provisioning);

    // Call post-download hook.
    if (StringUtils.isNotBlank(appProperties.getProvisioningPostHook())) {
      CommandLine cmdLine = CommandLine.parse(appProperties.getProvisioningPostHook());
      cmdLine.addArgument(finalFile.toFile().getAbsolutePath());
      cmdLine.addArgument(String.valueOf(!isInitialProvisioningDone()));

      if (appProperties.getProvisioningForkType()
        .equals(AppConstants.PROVISIONING_FORK_TYPE_SOFT)) {
        log.log(Level.FINE, "Calling (soft) post-provisioning hook {0}.",
          String.join(" ", cmdLine.toStrings()));
        DefaultExecutor executor = new DefaultExecutor();
        executor.execute(cmdLine);
        log.log(Level.FINE, "Finished post-provisioning (soft) hook execution.");
      } else if (appProperties.getProvisioningForkType()
        .equals(AppConstants.PROVISIONING_FORK_TYPE_HARD)) {
        log.log(Level.FINE, "Calling (hard) post-provisioning hook {0}.",
          String.join(" ", cmdLine.toStrings()));
        Runtime.getRuntime().exec(cmdLine.toStrings());
        log.log(Level.FINE, "Finished post-provisioning (hard) hook execution.");
      } else {
        log.log(Level.SEVERE, "Provisioning fork mode {0} is not supported.",
          appProperties.getProvisioningForkType());
      }
    }

    return provisioningInfoResponse.getPayload().getId();
  }
}
