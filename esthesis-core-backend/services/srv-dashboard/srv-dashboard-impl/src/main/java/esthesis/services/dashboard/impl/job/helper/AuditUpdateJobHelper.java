package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.audit.resource.AuditSystemResource;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.config.DashboardItemAuditConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateAudit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AuditUpdateJobHelper extends UpdateJobHelper {

	@Inject
	@RestClient
	AuditSystemResource auditSystemResource;

	public DashboardUpdateAudit refresh(DashboardEntity dashboardEntity,
		DashboardItemDTO item) throws JsonProcessingException {
		// Get item configuration & security checks.
		DashboardItemAuditConfiguration config = getConfig(DashboardItemAuditConfiguration.class, item);
		if (!checkSecurity(dashboardEntity, Category.AUDIT, Operation.READ, "")) {
			return null;
		}

		// Get audit entries and return update.
		return DashboardUpdateAudit.builder()
			.id(item.getId())
			.type(Type.AUDIT)
			.auditEntries(
				auditSystemResource.find(config.getEntries()).getContent().stream()
					.map(auditEntity -> Pair.of(auditEntity.getCreatedBy(), auditEntity.getMessage()))
					.toList())
			.build();
	}

	public void test(DashboardEntity dashboardEntity, DashboardItemDTO item)
	throws JsonProcessingException {
		DashboardUpdateAudit refresh = refresh(dashboardEntity, item);

		
	}

}
