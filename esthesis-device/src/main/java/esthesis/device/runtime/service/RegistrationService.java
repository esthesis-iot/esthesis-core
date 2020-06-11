package esthesis.device.runtime.service;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import esthesis.common.device.RegistrationRequest;
import esthesis.common.device.RegistrationResponse;
import esthesis.common.dto.DeviceMessage;
import esthesis.common.util.Base64E;
import esthesis.device.runtime.config.AppConstants;
import esthesis.device.runtime.config.AppProperties;
import esthesis.device.runtime.model.Registration;
import esthesis.device.runtime.repository.RegistrationRepository;
import esthesis.device.runtime.util.DeviceMessageUtil;
import esthesis.device.runtime.util.SecurityUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Validated
public class RegistrationService {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(RegistrationService.class.getName());

  private final AppProperties appProperties;
  private final RetryTemplate retryTemplate;
  private final SecurityUtil securityUtil;
  private final RestTemplate restTemplate;
  private final DeviceMessageUtil deviceMessageUtil;
  private final RegistrationRepository registrationRepository;

  public RegistrationService(AppProperties appProperties,
    RetryTemplate retryTemplate,
    SecurityUtil securityUtil, RestTemplate restTemplate,
    DeviceMessageUtil deviceMessageUtil,
    RegistrationRepository registrationRepository) {
    this.appProperties = appProperties;
    this.retryTemplate = retryTemplate;
    this.securityUtil = securityUtil;
    this.restTemplate = restTemplate;
    this.deviceMessageUtil = deviceMessageUtil;
    this.registrationRepository = registrationRepository;
  }

  public String register() throws IOException {
    // Check if registration is already done in the past.
    final List<Registration> registrations = registrationRepository.findAll();
    if (registrations.size() > 0) {
      return appProperties.getHardwareId();
    }

    // No registration in the past, so proceed with registration.
    LOGGER.log(Level.CONFIG, "Device is not registered, attempting registration now.");
    if (!appProperties.getRegistrationUrl().toLowerCase().startsWith("https://")) {
      LOGGER.log(Level.WARNING,
        "Registration is taking place over a non-encrypted protocol.");
    }

    // Attempt to register.
    String registrationUrl = appProperties.getRegistrationUrl() + AppConstants.URL_PS_REGISTER;
    LOGGER.log(Level.FINEST, "Attempting registration to {0}.", registrationUrl);

    DeviceMessage<RegistrationRequest> registrationRequest = new DeviceMessage<>();
    registrationRequest.setHardwareId(appProperties.getHardwareId());
    registrationRequest.setPayload(new RegistrationRequest()
      .setTags(appProperties.getTags())
      .setRepliesSigned(appProperties.isIncomingSigned())
      .setRepliesEncrypted(appProperties.isIncomingEncrypted())
    );

    // Sign and/or encrypt message according to preferences.
    deviceMessageUtil.prepareOutgoing(registrationRequest);

    // Fire up registration request.
    DeviceMessage<RegistrationResponse> registrationResponse = retryTemplate.execute(context ->
      restTemplate.exchange(registrationUrl, HttpMethod.POST, new HttpEntity(registrationRequest),
        new ParameterizedTypeReference<DeviceMessage<RegistrationResponse>>() {
        }).getBody());

    // Verify and/or decrypt incoming reply according to preferences.
    deviceMessageUtil.processIncoming(registrationResponse, RegistrationRequest.class);

    // Check if an MQTT server was received.
    if (registrationResponse.getPayload().getMqttServer() == null) {
      throw new QDoesNotExistException("Did not receive details for an MQTT server.");
    }

    // Registration was successful at this stage, so store registration details.
    LOGGER.log(Level.CONFIG, "Persisting registration information.");

    if (!securityUtil.areSecurityKeysPresent()) {
      LOGGER.log(Level.FINE, "Writing device private key at: {0}.",
        securityUtil.getPrivateKeyLocation());
      LOGGER.log(Level.FINEST, registrationResponse.getPayload().getPrivateKey());
      FileUtils.writeStringToFile(new File(securityUtil.getPrivateKeyLocation()),
        registrationResponse.getPayload().getPrivateKey(), StandardCharsets.UTF_8);

      LOGGER.log(Level.FINE, "Writing device public key at: {0}.",
        securityUtil.getPublicKeyLocation());
      LOGGER.log(Level.FINEST, registrationResponse.getPayload().getPublicKey());
      FileUtils.writeStringToFile(new File(securityUtil.getPublicKeyLocation()),
        registrationResponse.getPayload().getPublicKey(), StandardCharsets.UTF_8);
    }

    if (!securityUtil.isPSPublicKeyPresent()) {
      LOGGER.log(Level.FINE, "Writing platform public key at: {0}.",
        securityUtil.getPSPublicKeyLocation());
      LOGGER.log(Level.FINEST, registrationResponse.getPayload().getPsPublicKey());
      FileUtils.writeStringToFile(new File(securityUtil.getPSPublicKeyLocation()),
        registrationResponse.getPayload().getPsPublicKey(), StandardCharsets.UTF_8);
    }

    if (!securityUtil.isSessionKeyPresent()) {
      LOGGER.log(Level.FINE, "Writing session key at: {0}.",
        securityUtil.getSessionKeyLocation());
      LOGGER.log(Level.FINEST, registrationResponse.getPayload().getSessionKey());
      LOGGER.log(Level.FINEST,
        Arrays.toString(Base64E.decode(registrationResponse.getPayload().getSessionKey())));
      FileUtils.writeStringToFile(new File(securityUtil.getSessionKeyLocation()),
        registrationResponse.getPayload().getSessionKey(), StandardCharsets.UTF_8);
    }

    if (!securityUtil.isProvisioningKeyPresent()) {
      LOGGER.log(Level.FINE, "Writing provisioning key at: {0}.",
        securityUtil.getProvisioningKeyLocation());
      LOGGER.log(Level.FINEST, registrationResponse.getPayload().getProvisioningKey());
      LOGGER.log(Level.FINEST,
        Arrays.toString(Base64E.decode(registrationResponse.getPayload().getProvisioningKey())));
      FileUtils.writeStringToFile(new File(securityUtil.getProvisioningKeyLocation()),
        registrationResponse.getPayload().getProvisioningKey(), StandardCharsets.UTF_8);
    }

    if (!securityUtil.isRootCACertificatePresent()) {
      LOGGER.log(Level.FINE, "Writing root CA certificate at: {0}.",
        securityUtil.getRootCaCertificateLocation());
      LOGGER.log(Level.FINEST, registrationResponse.getPayload().getRootCaCertificate());
      LOGGER.log(Level.FINEST,
        Arrays.toString(Base64E.decode(registrationResponse.getPayload().getRootCaCertificate())));
      FileUtils.writeStringToFile(new File(securityUtil.getRootCaCertificateLocation()),
        registrationResponse.getPayload().getRootCaCertificate(), StandardCharsets.UTF_8);
    }

    if (!securityUtil.isCertificatePresent()) {
      LOGGER.log(Level.FINE, "Writing device certificate at: {0}.",
        securityUtil.getCertificateLocation());
      LOGGER.log(Level.FINEST, registrationResponse.getPayload().getCertificate());
      FileUtils.writeStringToFile(new File(securityUtil.getCertificateLocation()),
        registrationResponse.getPayload().getCertificate(), StandardCharsets.UTF_8);
    }

    // Save registration details.
    Registration registration = new Registration();
    registration.setProvisioningUrl(registrationResponse.getPayload().getProvisioningUrl());
    registration.setMqttServerIp(registrationResponse.getPayload().getMqttServer().getIpAddress());
    registration.setRegisteredOn(new Date());
    registrationRepository.save(registration);

    return appProperties.getHardwareId();
  }

  public String getProvisioningUrl() {
    final List<Registration> registration = registrationRepository.findAll();
    if (!registration.isEmpty()) {
      return registrationRepository.findAll().get(0).getProvisioningUrl();
    } else {
      LOGGER.log(Level.WARNING, "Requested provisioning URL, however registration of the device "
        + "is not yet completed.");
      return null;
    }
  }

  public String getEmbeddedMqttServer() {
    return registrationRepository.findAll().get(0).getMqttServerIp();
  }
}
