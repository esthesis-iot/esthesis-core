package esthesis.service.dashboard.dto;

import esthesis.core.common.AppConstants;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class DashboardItemDTO {

	private String id;

	@NotBlank
	private AppConstants.Dashboard.Type type;

	@NotBlank
	private String title;

	@NotBlank
	private String subtitle;

	private int columns;

	private int index;

	private String configuration;
}
