package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.config.DashboardItemTitleConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateTitle;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateTitle.DashboardUpdateTitleBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for updating the TITLE dashboard item.
 */
@Slf4j
@ApplicationScoped
public class TitleUpdateJobHelper extends UpdateJobHelper<DashboardUpdateTitle> {


	public DashboardUpdateTitle refresh(DashboardEntity dashboardEntity,
		DashboardItemDTO item) {
		DashboardUpdateTitleBuilder<?, ?> replyBuilder = DashboardUpdateTitle.builder()
			.id(item.getId())
			.type(Type.TITLE);

		try {
			// Get item configuration.
			DashboardItemTitleConfiguration config =
				getConfig(DashboardItemTitleConfiguration.class, item);

			// Get audit entries and return update.
			return replyBuilder.title(config.getTitle()).build();
		} catch (Exception e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.TITLE, item.getId(), e);
			return replyBuilder.isError(true).build();
		}
	}

}
