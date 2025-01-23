package esthesis.service.dashboard.entity;

import esthesis.core.common.entity.BaseEntity;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;

/**
 * Dashboard entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "Dashboard")
public class DashboardEntity extends BaseEntity {

	@NotBlank
	@Length(min = 3, max = 255)
	private String name;

	@Length(max = 2048)
	private String description;

	private boolean shared;
	private boolean home;
	private boolean displayLastUpdate;

	//TODO setup a kafka listener to remove user's dashboards when the user is deleted.
	private ObjectId ownerId;

	private List<DashboardItemDTO> items;

	@Min(1)
	@Max(300)
	private int updateInterval;

}
