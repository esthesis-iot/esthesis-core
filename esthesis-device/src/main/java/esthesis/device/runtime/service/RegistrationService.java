package esthesis.device.runtime.service;

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
import lombok.extern.java.Log;
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

@SuppressWarnings("unchecked")
@Service
@Validated
@Log
public class RegistrationService {

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
    if (!registrations.isEmpty()) {
      return appProperties.getHardwareId();
    }

    // No registration in the past, so proceed with registration.
    log.log(Level.CONFIG, "Device is not registered, attempting registration now.");
    if (!appProperties.getRegistrationUrl().toLowerCase().startsWith("https://")) {
      log.log(Level.WARNING,
        "Registration is taking place over a non-encrypted protocol.");
    }

    // Attempt to register.
    String registrationUrl = appProperties.getRegistrationUrl() + AppConstants.URL_PS_REGISTER;
    log.log(Level.FINEST, "Attempting registration to {0}.", registrationUrl);

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
    deviceMessageUtil.processIncoming(registrationResponse, RegistrationResponse.class);

    // Check if an MQTT server was received.
    if (registrationResponse.getPayload().getMqttServer() == null) {
      log.warning("Did not receive an MQTT server.");
    }

    // Registration was successful at this stage, so store registration details.
    log.log(Level.CONFIG, "Registration successful. Persisting registration information.");

    if (!securityUtil.areSecurityKeysPresent()) {
      log.log(Level.FINE, "Writing device private key at: {0}.",
        securityUtil.getPrivateKeyLocation());
      log.log(Level.FINEST, registrationResponse.getPayload().getPrivateKey());
      FileUtils.writeStringToFile(new File(securityUtil.getPrivateKeyLocation()),
        registrationResponse.getPayload().getPrivateKey(), StandardCharsets.UTF_8);

      log.log(Level.FINE, "Writing device public key at: {0}.",
        securityUtil.getPublicKeyLocation());
      log.log(Level.FINEST, registrationResponse.getPayload().getPublicKey());
      FileUtils.writeStringToFile(new File(securityUtil.getPublicKeyLocation()),
        registrationResponse.getPayload().getPublicKey(), StandardCharsets.UTF_8);
    }

    if (!securityUtil.isPSPublicKeyPresent()) {
      log.log(Level.FINE, "Writing platform public key at: {0}.",
        securityUtil.getPSPublicKeyLocation());
      log.log(Level.FINEST, registrationResponse.getPayload().getPsPublicKey());
      FileUtils.writeStringToFile(new File(securityUtil.getPSPublicKeyLocation()),
        registrationResponse.getPayload().getPsPublicKey(), StandardCharsets.UTF_8);
    }

    if (!securityUtil.isSessionKeyPresent()) {
      log.log(Level.FINE, "Writing session key at: {0}.",
        securityUtil.getSessionKeyLocation());
      log.log(Level.FINEST, registrationResponse.getPayload().getSessionKey());
      log.log(Level.FINEST,
        Arrays.toString(Base64E.decode(registrationResponse.getPayload().getSessionKey())));
      FileUtils.writeStringToFile(new File(securityUtil.getSessionKeyLocation()),
        registrationResponse.getPayload().getSessionKey(), StandardCharsets.UTF_8);
    }

    if (!securityUtil.isProvisioningKeyPresent()) {
      log.log(Level.FINE, "Writing provisioning key at: {0}.",
        securityUtil.getProvisioningKeyLocation());
      log.log(Level.FINEST, registrationResponse.getPayload().getProvisioningKey());
      log.log(Level.FINEST,
        Arrays.toString(Base64E.decode(registrationResponse.getPayload().getProvisioningKey())));
      FileUtils.writeStringToFile(new File(securityUtil.getProvisioningKeyLocation()),
        registrationResponse.getPayload().getProvisioningKey(), StandardCharsets.UTF_8);
    }

    if (!securityUtil.isRootCACertificatePresent()) {
      log.log(Level.FINE, "Writing root CA certificate at: {0}.",
        securityUtil.getRootCaCertificateLocation());
      log.log(Level.FINEST, registrationResponse.getPayload().getRootCaCertificate());
      log.log(Level.FINEST,
        Arrays.toString(Base64E.decode(registrationResponse.getPayload().getRootCaCertificate())));
      FileUtils.writeStringToFile(new File(securityUtil.getRootCaCertificateLocation()),
        registrationResponse.getPayload().getRootCaCertificate(), StandardCharsets.UTF_8);
    }

    if (!securityUtil.isCertificatePresent()) {
      log.log(Level.FINE, "Writing device certificate at: {0}.",
        securityUtil.getCertificateLocation());
      log.log(Level.FINEST, registrationResponse.getPayload().getCertificate());
      FileUtils.writeStringToFile(new File(securityUtil.getCertificateLocation()),
        registrationResponse.getPayload().getCertificate(), StandardCharsets.UTF_8);
    }

    // Save registration details.
    Registration registration = new Registration();
    registration.setProvisioningUrl(registrationResponse.getPayload().getProvisioningUrl());
    if (registrationResponse.getPayload().getMqttServer() != null) {
      registration
        .setMqttServerIp(registrationResponse.getPayload().getMqttServer().getIpAddress());
    }
    registration.setRegisteredOn(new Date());
    registrationRepository.save(registration);

    return appProperties.getHardwareId();
  }

  public String getProvisioningUrl() {
    final List<Registration> registration = registrationRepository.findAll();
    if (!registration.isEmpty()) {
      return registrationRepository.findAll().get(0).getProvisioningUrl();
    } else {
      log.log(Level.WARNING, "Requested provisioning URL, however registration of the device "
        + "is not yet completed.");
      return null;
    }
  }

  public String getEmbeddedMqttServer() {
    return registrationRepository.findAll().get(0).getMqttServerIp();
  }
}
