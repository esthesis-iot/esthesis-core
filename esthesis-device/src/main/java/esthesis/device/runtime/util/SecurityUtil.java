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

  private final static String KEYS_FOLDER = "keys";

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(SecurityUtil.class.getName());

  private final AppProperties appProperties;

  public SecurityUtil(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  public String getDevicePrivateKeyLocation() {
    return String.join(File.separator, appProperties.getSecureStorageRoot(), KEYS_FOLDER,
      "device-private-key.pem");
  }

  public String getRootCaCertificateLocation() {
    return String.join(File.separator, appProperties.getSecureStorageRoot(), KEYS_FOLDER,
      "root-ca.crt");
  }

  public String getDeviceCertificateLocation() {
    return String.join(File.separator, appProperties.getSecureStorageRoot(), KEYS_FOLDER,
      "device.crt");
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

  public String getDevicePrivateKey() {
    return readFile(getDevicePrivateKeyLocation());
  }

  public String getRootCACertificate() {
    return readFile(getRootCaCertificateLocation());
  }

  public String getDeviceCertificate() {
    return readFile(getDeviceCertificateLocation());
  }

  public boolean isRootCACertificatePresent() {
    return StringUtils.isNotBlank(getRootCACertificate());
  }

}
