package esthesis.service.device.entity;

import esthesis.common.data.DataUtils.ValueType;
import esthesis.core.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;

/**
 * An entity representing device attributes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "DeviceAttribute")
public class DeviceAttributeEntity extends BaseEntity {

	@NotNull
	private String deviceId;

	@NotNull
	private String attributeName;

	@NotNull
	private String attributeValue;

	@NotNull
	private ValueType attributeType;

	public DeviceAttributeEntity(ObjectId id, String deviceId) {
		this.setId(id);
		this.deviceId = deviceId;
	}
}
