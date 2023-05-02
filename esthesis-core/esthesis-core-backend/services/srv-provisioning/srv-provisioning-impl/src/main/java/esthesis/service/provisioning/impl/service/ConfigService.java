package esthesis.service.provisioning.impl.service;

import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_DIRECTORY;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_FILENAME;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_HOST;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_PASSIVE;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_PASSWORD;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_FTP_USERNAME;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_MINIO_CONFIG;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.HEADER_WEB_CONFIG;
import static org.apache.camel.component.minio.MinioConstants.MINIO_OPERATION;
import static org.apache.camel.component.minio.MinioConstants.OBJECT_NAME;

import esthesis.common.AppConstants.Provisioning.ConfigOption;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QMismatchException;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.repository.ProvisioningRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.minio.MinioOperations;
import org.bson.Document;

@Slf4j
@ApplicationScoped
public class ConfigService {


	@Inject
	ProvisioningRepository provisioningRepository;

	@SuppressWarnings("java:S1192")
	public void setupWebConfig(Exchange exchange) {
		// Get the provisioning package info.
		ProvisioningPackageEntity pp = provisioningRepository.parse(
			exchange.getIn().getBody(Document.class));
		if (pp == null) {
			throw new QDoesNotExistException("Could not find a provisioning package in this Exchange.");
		}

		// Extract the URL, username and password from the configuration.
		URL url = null;
		try {
			url = new URL(pp.fc(ConfigOption.WEB_URL).orElseThrow());
		} catch (MalformedURLException e) {
			throw new QMismatchException("The URL is malformed.", e);
		}
		String username = pp.fc(ConfigOption.WEB_USERNAME).orElse(null);
		String password = pp.fc(ConfigOption.WEB_PASSWORD).orElse(null);

		// Extract URL params.
		String protocol = url.getProtocol();
		String host = url.getHost();
		String port = url.getPort() > -1 ? ":" + url.getPort() : "";
		String filename = url.getFile();
		String auth = "";
		if (username != null && password != null) {
			auth = username + ":" + password + "@";
		}

		// Set exchange header with the details of the web server.
		String webConfig = protocol + "://" + auth + host + port + filename;
		exchange.getIn().setHeader(HEADER_WEB_CONFIG, webConfig);
	}

	public void setupFtpConfig(Exchange exchange) {
		// Get the provisioning package info.
		ProvisioningPackageEntity pp = provisioningRepository.parse(
			exchange.getIn().getBody(Document.class));
		if (pp == null) {
			throw new QDoesNotExistException("Could not find a provisioning package in this Exchange.");
		}

		// Set Camel FTP configuration.
		// FTP host + port.
		if (pp.fc(ConfigOption.FTP_PORT).isPresent()) {
			String host =
				pp.fc(ConfigOption.FTP_HOST).orElseThrow() + ":" + pp.fc(ConfigOption.FTP_PORT)
					.orElseThrow();
			log.debug("Setting FTP host to '{}'.", host);
			exchange.getIn().setHeader(HEADER_FTP_HOST, host);
		} else {
			String host = pp.fc(ConfigOption.FTP_HOST).orElseThrow();
			exchange.getIn().setHeader(HEADER_FTP_HOST, host);
		}

		// FTP username + password.
		if (pp.fc(ConfigOption.FTP_USERNAME).isPresent() && pp.fc(ConfigOption.FTP_PASSWORD)
			.isPresent()) {
			String username = pp.fc(ConfigOption.FTP_USERNAME).orElseThrow();
			exchange.getIn().setHeader(HEADER_FTP_USERNAME, username);
			log.debug("Setting FTP username to '{}'.", username);

			String password = pp.fc(ConfigOption.FTP_PASSWORD).orElseThrow();
			exchange.getIn().setHeader(HEADER_FTP_PASSWORD, password);
			log.debug("Setting FTP password to '{}'.", password);
		}

		// FTP passive.
		exchange.getIn().setHeader(HEADER_FTP_PASSIVE, pp.fc(ConfigOption.FTP_PASSIVE).orElse("false"));

		// FTP path.
		if (pp.fc(ConfigOption.FTP_PATH).isPresent()) {
			Path path = Path.of(pp.fc(ConfigOption.FTP_PATH).orElseThrow());
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

	public void setupMinioConfig(Exchange exchange) {
		// Get the provisioning package info.
		ProvisioningPackageEntity pp = provisioningRepository.parse(
			exchange.getIn().getBody(Document.class));
		if (pp == null) {
			throw new QDoesNotExistException("Could not find a provisioning package in this Exchange.");
		}

		// Set up MinIO header options.
		exchange.getIn().setHeader(MINIO_OPERATION, MinioOperations.getObject);
		exchange.getIn().setHeader(OBJECT_NAME, pp.fc(ConfigOption.MINIO_OBJECT).orElseThrow());

		// Set up the MinIO Camel endpoint.
		String minioConfig = "minio://" + pp.fc(ConfigOption.MINIO_BUCKET).orElseThrow();
		minioConfig += "?endpoint=" + pp.fc(ConfigOption.MINIO_URL).orElseThrow();
		if (pp.fc(ConfigOption.MINIO_ACCESS_KEY).isPresent() && pp.fc(ConfigOption.MINIO_SECRET_KEY)
			.isPresent()) {
			minioConfig += "&accessKey=" + pp.fc(ConfigOption.MINIO_ACCESS_KEY).orElseThrow()
				+ "&secretKey=" + pp.fc(ConfigOption.MINIO_SECRET_KEY).orElseThrow();
		}
		exchange.getIn().setHeader(HEADER_MINIO_CONFIG, minioConfig);
	}
}
