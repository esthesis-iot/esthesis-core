package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.audit.resource.AuditSystemResource;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.config.DashboardItemAuditConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateAudit;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateAudit.DashboardUpdateAuditBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class AuditUpdateJobHelper extends UpdateJobHelper<DashboardUpdateAudit> {

	@Inject
	@RestClient
	AuditSystemResource auditSystemResource;

	public DashboardUpdateAudit refresh(DashboardEntity dashboardEntity,
		DashboardItemDTO item) {
		DashboardUpdateAuditBuilder<?, ?> replyBuilder = DashboardUpdateAudit.builder()
			.id(item.getId())
			.type(Type.AUDIT);

		try {
			// Get item configuration & security checks.
			DashboardItemAuditConfiguration config = getConfig(DashboardItemAuditConfiguration.class,
				item);
			if (!checkSecurity(dashboardEntity, Category.AUDIT, Operation.READ, "")) {
				return replyBuilder.isSecurityError(true).isError(true).build();
			}

			// Get audit entries and return update.
			return replyBuilder
				.auditEntries(
					auditSystemResource.find(config.getEntries()).getContent().stream()
						.map(auditEntity -> Pair.of(auditEntity.getCreatedBy(), auditEntity.getMessage()))
						.toList())
				.build();
		} catch (Exception e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.AUDIT, item.getId(), e);
			return replyBuilder.isError(true).build();
		}
	}

}
