package esthesis.service.provisioning.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import esthesis.common.AppConstants.Provisioning.Type;
import esthesis.common.entity.BaseEntity;
import esthesis.common.jackson.MongoInstantDeserializer;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jboss.resteasy.reactive.RestForm;

@Data
@Builder()
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
	@NotNull
	@RestForm
	private boolean available;

	// The version of this package, following semantic versioning principles. This allows esthesis
	// to automatically determine which package is "next" to be downloaded given a specific package
	// version, helpful during device firmware updates. If semantic versioning is not followed,
	// switch this feature off in the settings of the application.
	@NotNull
	@RestForm
	private String version;

	// A reference to a version which is a prerequisite for this version to be installed.
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

	@JsonDeserialize(using = MongoInstantDeserializer.class)
	private Instant createdOn;

}
