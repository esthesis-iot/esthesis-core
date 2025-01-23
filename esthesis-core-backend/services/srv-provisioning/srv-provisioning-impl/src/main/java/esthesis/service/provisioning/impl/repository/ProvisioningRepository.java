package esthesis.service.provisioning.impl.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.exception.QMismatchException;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.bson.Document;

/**
 * Quarkus Panache repository for {@link ProvisioningPackageEntity}.
 */
@ApplicationScoped
public class ProvisioningRepository implements PanacheMongoRepository<ProvisioningPackageEntity> {

	@Inject
	ObjectMapper mapper;

	/**
	 * A convenience method to convert a Document of type {@link ProvisioningPackageEntity} to the
	 * underlying ProvisioningPackage class.
	 *
	 * @param doc The document to convert.
	 * @return The converted ProvisioningPackageEntity.
	 */
	public ProvisioningPackageEntity parse(Document doc) {
		String json = doc.toJson();
		try {
			ProvisioningPackageEntity provisioningPackageEntity = mapper.readValue(json,
				ProvisioningPackageEntity.class);
			provisioningPackageEntity.setId(doc.getObjectId("_id"));
			return provisioningPackageEntity;
		} catch (JsonProcessingException e) {
			throw new QMismatchException("Could not convert ProvisioningPackage Document to "
				+ "ProvisioningPackage class.", e);
		}
	}

	/**
	 * Find all provisioning packages that match at least one of the given tags.
	 *
	 * @param tagIds The tag ids to match.
	 * @return A list of provisioning packages that match at least one of the given tags.
	 */
	public List<ProvisioningPackageEntity> findByTagIds(List<String> tagIds) {
		return list("tags in ?1", tagIds.toArray());
	}

	/**
	 * Checks if a version is in a list of tags.
	 *
	 * @param version The version to check.
	 * @param tags    The tags to check.
	 * @return True if the version is in the tags, false otherwise.
	 */
	public boolean versionInTags(String version, List<String> tags) {
		List<ProvisioningPackageEntity> matchedPackages = findByTagIds(tags);
		return matchedPackages.stream().anyMatch(p -> p.getVersion().equals(version));
	}
}
