package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.about.resource.AboutSystemResource;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateAbout;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateAbout.DashboardUpdateAboutBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Helper class for updating the ABOUT dashboard item.
 */
@Slf4j
@ApplicationScoped
public class AboutUpdateJobHelper extends UpdateJobHelper<DashboardUpdateAbout> {

	@Inject
	@RestClient
	AboutSystemResource aboutSystemResource;

	public DashboardUpdateAbout refresh(DashboardEntity dashboardEntity, DashboardItemDTO item) {
		DashboardUpdateAboutBuilder<?, ?> replyBuilder = DashboardUpdateAbout.builder()
			.id(item.getId())
			.type(Type.ABOUT);

		try {
			// Security checks.
			if (!checkSecurity(dashboardEntity, Category.ABOUT, Operation.READ, "")) {
				return replyBuilder.isError(true).isSecurityError(true).build();
			}

			// Get data.
			AboutGeneralDTO generalInfo = aboutSystemResource.getGeneralInfo();

			// Return update.
			return replyBuilder
				.gitBuildTime(generalInfo.getGitBuildTime())
				.gitCommitId(generalInfo.getGitCommitId())
				.gitCommitIdAbbrev(generalInfo.getGitCommitIdAbbrev())
				.gitVersion(generalInfo.getGitVersion())
				.gitBuildTime(generalInfo.getGitBuildTime())
				.build();
		} catch (Exception e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.ABOUT, item.getId(), e);
			return replyBuilder.isError(true).build();
		}
	}
}
