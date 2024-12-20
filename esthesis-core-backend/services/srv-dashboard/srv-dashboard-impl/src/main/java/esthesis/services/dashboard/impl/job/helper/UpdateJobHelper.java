package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.security.resource.SecuritySystemResource;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RestClient;

public abstract class UpdateJobHelper<T> {

	@Inject
	ObjectMapper objectMapper;

	@Inject
	@RestClient
	SecuritySystemResource securitySystemResource;

	// A map representing security checks. To not repeat security checks each time the value of a
	// dashboard item is refreshed, the result of the first security check for the specific type
	// of dashboard item is stored in this map. For as long as this job is active, the security
	// check result is considered valid (i.e. if user's permissions change, the updated security
	// permissions will only be taken into account the next time the user will view the dashboard).
	//
	// The key of the map is a unique entry constructed for the specific dashboard item being checked,
	// while the value is a boolean representing the result of the security check.
	private final Map<String, Boolean> securityChecks = new HashMap<>();

	protected <C> C getConfig(Class<C> configurationClass, DashboardItemDTO item)
	throws JsonProcessingException {
		return objectMapper.readValue(item.getConfiguration(), configurationClass);
	}

	protected boolean checkSecurity(DashboardEntity dashboardEntity, Category category,
		Operation operation, String hardwareId) {
		String securityKey = String.join(":", category.toString(), operation.toString(), hardwareId,
			dashboardEntity.getOwnerId().toHexString());
		if (securityChecks.containsKey(securityKey) && Boolean.FALSE.equals(
			securityChecks.get(securityKey))) {
			return false;
		} else if (!securityChecks.containsKey(securityKey)) {
			Boolean check = securitySystemResource.isPermitted(category, operation, hardwareId,
				dashboardEntity.getOwnerId());
			securityChecks.put(securityKey, check);
		}
		return Boolean.TRUE.equals(securityChecks.get(securityKey));
	}

	public abstract T refresh(DashboardEntity dashboardEntity, DashboardItemDTO item)
	throws JsonProcessingException;
}
