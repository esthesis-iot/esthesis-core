package esthesis.device.runtime.util;

import esthesis.device.runtime.config.AppProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class SecurityUtil {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(SecurityUtil.class.getName());

  private final AppProperties appProperties;

  public SecurityUtil(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  public String getPrivateKeyLocation() {
    return appProperties.getSecureStorageRoot() + File.separator + "device-private-key.pem";
  }

  public String getPublicKeyLocation() {
    return appProperties.getStorageRoot() + File.separator + "device-public-key.pem";
  }

  public String getPSPublicKeyLocation() {
    return appProperties.getStorageRoot() + File.separator + "platform-public-key.pem";
  }

  public String getSessionKeyLocation() {
    return appProperties.getSecureStorageRoot() + File.separator + "device-session.key";
  }

  public String getProvisioningKeyLocation() {
    return appProperties.getSecureStorageRoot() + File.separator + "platform-provisioning.key";
  }

  public String getRootCaCertificateLocation() {
    return appProperties.getStorageRoot() + File.separator + "root-ca.crt";
  }

  public String getCertificateLocation() {
    return appProperties.getStorageRoot() + File.separator + "device.crt";
  }

  private String readFile(String fileLocation) {
    File file = new File(fileLocation);
    if (file.exists()) {
      try {
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, MessageFormat.format("Could not read file {0}.", file), e);
        return null;
      }
    } else {
      return null;
    }
  }

  public String getPublicKey() {
    return readFile(getPublicKeyLocation());
  }

  public String getPSPublicKey() {
    return readFile(getPSPublicKeyLocation());
  }

  public String getPrivateKey() {
    return readFile(getPrivateKeyLocation());
  }

  public String getSessionKey() {
    return readFile(getSessionKeyLocation());
  }

  public String getRootCACertificate() {
    return readFile(getRootCaCertificateLocation());
  }

  public String getProvisioningKey() {
    return readFile(getProvisioningKeyLocation());
  }

  public String getCertificate() {
    return readFile(getCertificateLocation());
  }

  /**
   * Checks whether device public, private and session keys are available.
   *
   * @return Returns true if both public, private and session keys are available, or false if any of
   * them is missing.
   */
  public boolean areSecurityKeysPresent() {
    return (StringUtils.isNotBlank(getPrivateKey())
      && StringUtils.isNotBlank(getPublicKey())
      && StringUtils.isNotBlank(getSessionKey()));
  }

  /**
   * Checks whether the public key of the PS is available.
   *
   * @return Returns true if the PS public key is available, false otherwise.
   */
  public boolean isPSPublicKeyPresent() {
    return StringUtils.isNotBlank(getPSPublicKey());
  }

  public boolean isSessionKeyPresent() {
    return StringUtils.isNotBlank(getSessionKey());
  }

  public boolean isProvisioningKeyPresent() {
    return StringUtils.isNotBlank(getProvisioningKey());
  }

  public boolean isRootCACertificatePresent() {
    return StringUtils.isNotBlank(getRootCACertificate());
  }

  public boolean isCertificatePresent() {
    return StringUtils.isNotBlank(getCertificate());
  }

}
