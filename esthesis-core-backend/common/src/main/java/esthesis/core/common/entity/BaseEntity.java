package esthesis.core.common.entity;

import java.io.Serializable;
import lombok.Data;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestForm;

/**
 * Base entity class for all entities.
 */
@Data
public abstract class BaseEntity implements Serializable {

	@RestForm
	private ObjectId id;
}
