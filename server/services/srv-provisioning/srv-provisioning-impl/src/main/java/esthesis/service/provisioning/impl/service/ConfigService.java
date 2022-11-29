package esthesis.service.provisioning.impl.service;

import esthesis.common.AppConstants.Provisioning.OptionsFtp;
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
    if (pp.fc(OptionsFtp.FTP_PORT).isPresent()) {
      String host =
          pp.fc(OptionsFtp.FTP_HOST).orElseThrow() + ":" + pp.fc(OptionsFtp.FTP_PORT).get();
      log.debug("Setting FTP host to '{}'.", host);
      exchange.getIn().setHeader("host", host);
    } else {
      String host = pp.fc(OptionsFtp.FTP_HOST).orElseThrow();
      exchange.getIn().setHeader("host", host);
    }

    if (pp.fc(OptionsFtp.FTP_USERNAME).isPresent() && pp.fc(OptionsFtp.FTP_PASSWORD).isPresent()) {
      String username = pp.fc(OptionsFtp.FTP_USERNAME).orElseThrow();
      exchange.getIn().setHeader("username", username);
      log.debug("Setting FTP username to '{}'.", username);

      String password = pp.fc(OptionsFtp.FTP_PASSWORD).orElseThrow();
      exchange.getIn().setHeader("password", password);
      log.debug("Setting FTP password to '{}'.", password);
    }

    if (pp.fc(OptionsFtp.FTP_PASSIVE).isPresent()) {
      String passive = pp.fc(OptionsFtp.FTP_PASSIVE).get();
      exchange.getIn().setHeader("passive", passive);
      log.debug("Setting FTP passive mode to '{}'.", passive);
    } else {
      exchange.getIn().setHeader("passive", "false");
    }

    if (pp.fc(OptionsFtp.FTP_PATH).isPresent()) {
      Path path = Path.of(pp.fc(OptionsFtp.FTP_PATH).get());
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
