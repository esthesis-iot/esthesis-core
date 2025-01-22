package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDatetime;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDatetime.DashboardUpdateDatetimeBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class DatetimeUpdateJobHelper extends UpdateJobHelper<DashboardUpdateDatetime> {

	public DashboardUpdateDatetime refresh(DashboardEntity dashboardEntity,
		DashboardItemDTO item) {
		DashboardUpdateDatetimeBuilder<?, ?> replyBuilder = DashboardUpdateDatetime.builder()
			.id(item.getId())
			.type(Type.DATETIME);

		try {
			return replyBuilder.serverDate(System.currentTimeMillis()).build();
		} catch (Exception e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.TITLE, item.getId(), e);
			return replyBuilder.isError(true).build();
		}
	}

}
