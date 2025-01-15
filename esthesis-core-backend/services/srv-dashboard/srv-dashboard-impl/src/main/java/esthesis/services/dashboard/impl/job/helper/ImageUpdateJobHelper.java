package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.service.audit.resource.AuditSystemResource;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.config.DashboardItemImageConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateImage;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateImage.DashboardUpdateImageBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class ImageUpdateJobHelper extends UpdateJobHelper<DashboardUpdateImage> {

	@Inject
	@RestClient
	AuditSystemResource auditSystemResource;

	public DashboardUpdateImage refresh(DashboardEntity dashboardEntity,
		DashboardItemDTO item) {
		DashboardUpdateImageBuilder<?, ?> replyBuilder = DashboardUpdateImage.builder()
			.id(item.getId())
			.type(Type.IMAGE);

		try {
			// Get item configuration.
			DashboardItemImageConfiguration config =
				getConfig(DashboardItemImageConfiguration.class, item);

			// Get audit entries and return update.
			return replyBuilder.imageUrl(config.getImageUrl()).build();
		} catch (Exception e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.IMAGE, item.getId(), e);
			return replyBuilder.isError(true).build();
		}
	}

}
