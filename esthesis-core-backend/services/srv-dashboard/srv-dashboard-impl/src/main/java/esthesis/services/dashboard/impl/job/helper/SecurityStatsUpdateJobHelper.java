package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.security.dto.StatsDTO;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateSecurityStats;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateSecurityStats.DashboardUpdateSecurityStatsBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class SecurityStatsUpdateJobHelper extends UpdateJobHelper<DashboardUpdateSecurityStats> {

	@Inject
	@RestClient
	SecuritySystemResource securitySystemResource;

	public DashboardUpdateSecurityStats refresh(DashboardEntity dashboardEntity,
		DashboardItemDTO item) {
		DashboardUpdateSecurityStatsBuilder<?, ?> replyBuilder = DashboardUpdateSecurityStats.builder()
			.id(item.getId()).type(Type.SECURITY_STATS);

		try {
			if (!checkSecurity(dashboardEntity, Category.USERS, Operation.READ, "*") ||
				!checkSecurity(dashboardEntity, Category.GROUPS, Operation.READ, "*") ||
				!checkSecurity(dashboardEntity, Category.POLICIES, Operation.READ, "*") ||
				!checkSecurity(dashboardEntity, Category.AUDIT, Operation.READ, "*")) {
				return replyBuilder.isSecurityError(true).build();
			}

			// Get value and return update.
			StatsDTO statsDTO = securitySystemResource.stats();

			return replyBuilder
				.users(statsDTO.getUsers())
				.groups(statsDTO.getGroups())
				.roles(statsDTO.getRoles())
				.policies(statsDTO.getPolicies())
				.audits(statsDTO.getAudits())
				.build();
		} catch (Exception e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.SECURITY_STATS, item.getId(),
				e);
			return replyBuilder.isError(true).build();
		}
	}
}
