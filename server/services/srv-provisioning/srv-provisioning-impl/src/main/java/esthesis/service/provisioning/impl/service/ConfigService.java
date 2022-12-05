package esthesis.service.provisioning.impl.service;

import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_HOST;
import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_PASSIVE;
import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_PASSWORD;
import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_PATH;
import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_PORT;
import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_USERNAME;

import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QMismatchException;
import esthesis.service.provisioning.dto.ProvisioningPackage;
import esthesis.service.provisioning.impl.repository.ProvisioningRepository;
import java.nio.file.Path;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.bson.Document;

@Slf4j
@ApplicationScoped
public class ConfigService {

  @Inject
  ProvisioningRepository provisioningRepository;

  public void setupFtpConfig(Exchange exchange) {
    // Get the provisioning package info.
    ProvisioningPackage pp = provisioningRepository.parse(exchange.getIn().getBody(Document.class));
    if (pp == null) {
      throw new QDoesNotExistException("Could not find a provisioning package in this Exchange.");
    }

    // Set Camel FTP configuration.
    if (pp.fc(FTP_PORT).isPresent()) {
      String host = pp.fc(FTP_HOST).orElseThrow() + ":" + pp.fc(FTP_PORT).get();
      log.debug("Setting FTP host to '{}'.", host);
      exchange.getIn().setHeader("host", host);
    } else {
      String host = pp.fc(FTP_HOST).orElseThrow();
      exchange.getIn().setHeader("host", host);
    }

    if (pp.fc(FTP_USERNAME).isPresent() && pp.fc(FTP_PASSWORD).isPresent()) {
      String username = pp.fc(FTP_USERNAME).orElseThrow();
      exchange.getIn().setHeader("username", username);
      log.debug("Setting FTP username to '{}'.", username);

      String password = pp.fc(FTP_PASSWORD).orElseThrow();
      exchange.getIn().setHeader("password", password);
      log.debug("Setting FTP password to '{}'.", password);
    }

    exchange.getIn().setHeader("passive", pp.fc(FTP_PASSIVE).orElse("false"));

    if (pp.fc(FTP_PATH).isPresent()) {
      Path path = Path.of(pp.fc(FTP_PATH).get());
      String directory = path.getParent().toString();
      if (directory.startsWith("/")) {
        directory = directory.substring(1);
      }
      String filename = path.getFileName().toString();
      exchange.getIn().setHeader("directory", directory);
      exchange.getIn().setHeader("filename", filename);
      log.debug("Setting FTP directory to '{}' and FTP filename to '{}'.", directory, filename);
    } else {
      throw new QMismatchException("FTP path is not specified for provisioning package '{}'.",
          pp.getId());
    }
  }
}
