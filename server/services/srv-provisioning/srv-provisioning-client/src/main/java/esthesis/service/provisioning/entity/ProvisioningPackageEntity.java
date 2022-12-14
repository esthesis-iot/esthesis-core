package esthesis.service.provisioning.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import esthesis.common.AppConstants.Provisioning.CacheStatus;
import esthesis.common.AppConstants.Provisioning.ConfigOption;
import esthesis.common.AppConstants.Provisioning.Type;
import esthesis.common.entity.BaseEntity;
import esthesis.common.jackson.MongoInstantDeserializer;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jboss.resteasy.reactive.RestForm;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "ProvisioningPackage")
public class ProvisioningPackageEntity extends BaseEntity {

  @NotNull
  @RestForm
  private String name;

  @RestForm
  private String description;

  // Whether this package is available for distribution or not.
  @RestForm
  private boolean available;

  // The version of this package, following semantic versioning principles. This allows esthesis
  // to automatically determine which package is "next" to be downloaded given a specific package
  // version, helpful during device firmware updates.
  @RestForm
  private String version;

  // A reference to a version which is a prerequisite for this version to be installed. This is
  // also taken into account when esthesis tries to determine which is the next version of a
  // package to be installed on a device given the current version of the device.
  @RestForm
  private String prerequisiteVersion;

  // The size (in bytes) of this package.
  private long size;

  // The content type of the uploaded file.
  private String contentType;

  private String filename;

  // The Tag ids associated with this package.
  @RestForm
  private List<String> tags;

  // The attributes of a package are sent to the device during a firmware update. Attributes
  // can be anything of value to the device during the firmware upgrade process, however take
  // into account that attributes are passed as an argument to the command line handling the
  // firmware update process, so their format has to be appropriate.
  @RestForm
  private String attributes;

  // A hash (SHA256) for the binary content of this package.
  @RestForm
  private String sha256;

  // The type of this package indicating where the binary payload resides.
  @RestForm
  private Type type;

  // A comma-separated list of key=value pairs with package type specific configuration.
  @RestForm
  private String typeSpecificConfiguration;

  // Indicates what's the current status of a provisioning package's cache.
  private CacheStatus cacheStatus;

  // A log output of the caching result.
  private String log;

  @JsonDeserialize(using = MongoInstantDeserializer.class)
  private Instant createdOn;

  /**
   * Find Config (fc), is a convenience method to return a value from typeSpecificConfiguration.
   *
   * @param key The key to look for.
   */
  public Optional<String> fc(ConfigOption key) {
    return Arrays.stream(getTypeSpecificConfiguration().split(","))
        .filter(s -> s.startsWith(key + "="))
        .findFirst()
        .map(s -> s.substring(s.indexOf("=") + 1));
  }

}
