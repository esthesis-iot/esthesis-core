package esthesis.service.provisioning.impl.service;

import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_HOST;
import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_PASSIVE;
import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_PASSWORD;
import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_PATH;
import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_PORT;
import static esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp.FTP_USERNAME;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_DIRECTORY;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_FILENAME;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_HOST;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_PASSIVE;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_PASSWORD;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_USERNAME;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_WEB_CONFIG;

import esthesis.common.AppConstants.Provisioning.ConfigOptions.Web;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QMismatchException;
import esthesis.service.provisioning.dto.ProvisioningPackage;
import esthesis.service.provisioning.impl.repository.ProvisioningRepository;
import java.net.MalformedURLException;
import java.net.URL;
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

  public void setupWebConfig(Exchange exchange) {
    // Get the provisioning package info.
    ProvisioningPackage pp = provisioningRepository.parse(exchange.getIn().getBody(Document.class));
    if (pp == null) {
      throw new QDoesNotExistException("Could not find a provisioning package in this Exchange.");
    }

    // Extract the URL, username and password from the configuration.
    URL url = null;
    try {
      url = new URL(pp.fc(Web.WEB_URL).orElseThrow());
    } catch (MalformedURLException e) {
      throw new QMismatchException("The URL is malformed.", e);
    }
    String username = pp.fc(Web.WEB_USERNAME).orElse(null);
    String password = pp.fc(Web.WEB_PASSWORD).orElse(null);

    // Extract URL params.
    String protocol = url.getProtocol();
    String host = url.getHost();
    String port = url.getPort() > -1 ? ":" + url.getPort() : "";
    String filename = url.getFile();
    String auth = "";
    if (username != null & password != null) {
      auth = username + ":" + password + "@";
    }

    // Set exchange header with the details of the web server.
    String webConfig = protocol + "://" + auth + host + port + filename;
    exchange.getIn().setHeader(HEADER_WEB_CONFIG, webConfig);
  }

  public void setupFtpConfig(Exchange exchange) {
    // Get the provisioning package info.
    ProvisioningPackage pp = provisioningRepository.parse(exchange.getIn().getBody(Document.class));
    if (pp == null) {
      throw new QDoesNotExistException("Could not find a provisioning package in this Exchange.");
    }

    // Set Camel FTP configuration.
    // FTP host + port.
    if (pp.fc(FTP_PORT).isPresent()) {
      String host = pp.fc(FTP_HOST).orElseThrow() + ":" + pp.fc(FTP_PORT).get();
      log.debug("Setting FTP host to '{}'.", host);
      exchange.getIn().setHeader(HEADER_FTP_HOST, host);
    } else {
      String host = pp.fc(FTP_HOST).orElseThrow();
      exchange.getIn().setHeader(HEADER_FTP_HOST, host);
    }

    // FTP username + password.
    if (pp.fc(FTP_USERNAME).isPresent() && pp.fc(FTP_PASSWORD).isPresent()) {
      String username = pp.fc(FTP_USERNAME).orElseThrow();
      exchange.getIn().setHeader(HEADER_FTP_USERNAME, username);
      log.debug("Setting FTP username to '{}'.", username);

      String password = pp.fc(FTP_PASSWORD).orElseThrow();
      exchange.getIn().setHeader(HEADER_FTP_PASSWORD, password);
      log.debug("Setting FTP password to '{}'.", password);
    }

    // FTP passive.
    exchange.getIn().setHeader(HEADER_FTP_PASSIVE, pp.fc(FTP_PASSIVE).orElse("false"));

    // FTP path.
    if (pp.fc(FTP_PATH).isPresent()) {
      Path path = Path.of(pp.fc(FTP_PATH).get());
      String directory = path.getParent().toString();
      if (directory.startsWith("/")) {
        directory = directory.substring(1);
      }
      String filename = path.getFileName().toString();
      exchange.getIn().setHeader(HEADER_FTP_DIRECTORY, directory);
      exchange.getIn().setHeader(HEADER_FTP_FILENAME, filename);
      log.debug("Setting FTP directory to '{}' and FTP filename to '{}'.", directory, filename);
    } else {
      throw new QMismatchException("FTP path is not specified for provisioning package '{}'.",
          pp.getId());
    }
  }
}
