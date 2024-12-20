package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.about.resource.AboutSystemResource;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateAbout;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AboutUpdateJobHelper extends UpdateJobHelper<DashboardUpdateAbout> {

	@Inject
	@RestClient
	AboutSystemResource aboutSystemResource;

	public DashboardUpdateAbout refresh(DashboardEntity dashboardEntity,
		DashboardItemDTO item) throws JsonProcessingException {

		// Security checks.
		if (!checkSecurity(dashboardEntity, Category.ABOUT, Operation.READ, "")) {
			return null;
		}

		// Get data.
		AboutGeneralDTO generalInfo = aboutSystemResource.getGeneralInfo();

		// Return update.
		return DashboardUpdateAbout.builder()
			.id(item.getId())
			.type(Type.ABOUT)
			.gitBuildTime(generalInfo.getGitBuildTime())
			.gitCommitId(generalInfo.getGitCommitId())
			.gitCommitIdAbbrev(generalInfo.getGitCommitIdAbbrev())
			.gitBuildTime(generalInfo.getGitBuildTime())
			.build();
	}
}
