package esthesis.device.runtime.repository;

import esthesis.device.runtime.config.AppProperties;
import esthesis.device.runtime.model.Provisioning;
import esthesis.device.runtime.model.Registration;
import io.jsondb.JsonDBTemplate;
import javax.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.logging.Level;

@Component
@Log
public class JsonDbRepository {
  private final AppProperties appProperties;
  // Package name where POJO's are present.
  private final String baseScanPackage = "esthesis.device.runtime.model";
  private JsonDBTemplate jsonDBTemplate;

  public JsonDbRepository(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  @PostConstruct
  public void init() {
    // The location of JSONDB.
    String dbFilesLocation = Paths.get(appProperties.getStorageRoot(), "db").toString();
    log.log(Level.INFO,  "Initialising JSONDB on {0}.", dbFilesLocation);
    jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage);

    // Initialise collections.
    if (!jsonDBTemplate.collectionExists(Registration.class)) {
      jsonDBTemplate.createCollection(Registration.class);
    }
    if (!jsonDBTemplate.collectionExists(Provisioning.class)) {
      jsonDBTemplate.createCollection(Provisioning.class);
    }
  }

  public JsonDBTemplate getTemplate() {
    return jsonDBTemplate;
  }
}
