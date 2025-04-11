package esthesis.services.dashboard.impl.job.helper;

import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDatetime;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static esthesis.core.common.AppConstants.Dashboard.Type.DATETIME;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class DatetimeUpdateJobHelperTest {


	@Inject
	DatetimeUpdateJobHelper datetimeUpdateJobHelper;

	@Inject
	TestHelper testHelper;


	@Test
	void refresh() {
		// Arrange a dashboard and a datetime item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-datetime-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-datetime-item", 0, DATETIME);
		DashboardUpdateDatetime dashboardUpdateDatetime = datetimeUpdateJobHelper.refresh(dashboardEntity, item);

		// Assert the server date was set.
		assertTrue(dashboardUpdateDatetime.getServerDate() > 0L);
	}
}
