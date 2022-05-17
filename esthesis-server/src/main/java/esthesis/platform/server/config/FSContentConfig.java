package esthesis.platform.server.config;

import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.content.fs.config.EnableFilesystemStores;
import org.springframework.content.fs.io.FileSystemResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

@Log
@Configuration
@EnableFilesystemStores
public class FSContentConfig {

  private final AppProperties appProperties;

  public FSContentConfig(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  @Bean
  File filesystemRoot() {
    // Set the root fs for provisioning packages.
    Path fsRoot;
    if (StringUtils.isBlank(appProperties.getFsRoot())) {
      fsRoot = Paths.get(System.getProperty("user.home"), ".esthesis");
      log.log(Level.WARNING, "Filesystem root is not set, setting it under {0}.", fsRoot);
    } else {
      fsRoot = Paths.get(appProperties.getFsRoot());
      log.log(Level.INFO, "Setting root filesystem under {0}.", fsRoot);
    }

    // Update application properties with the path for provisioning packages.
    appProperties.setFsRoot(fsRoot.toString());
    appProperties.setFsTmpRoot(
      Paths.get(fsRoot.toFile().getAbsolutePath(), ".tmp").toFile().getAbsolutePath());
    appProperties.setFsProvisioningRoot(Paths.get(fsRoot.toFile().getAbsolutePath(),
      "provisioning").toFile().getAbsolutePath());

    // Create parent directories if not exist.
    try {
      Files.createDirectories(Paths.get(appProperties.getFsRoot()));
      Files.createDirectories(Paths.get(appProperties.getFsProvisioningRoot()));
      Files.createDirectories(Paths.get(appProperties.getFsTmpRoot()));
    } catch (IOException e) {
      log.log(Level.SEVERE, "Could not create parent directories for {0}.", fsRoot);
    }

    return new File(appProperties.getFsProvisioningRoot());
  }

  @Bean
  FileSystemResourceLoader fileSystemResourceLoader() {
    return new FileSystemResourceLoader(filesystemRoot().getAbsolutePath());
  }

}
