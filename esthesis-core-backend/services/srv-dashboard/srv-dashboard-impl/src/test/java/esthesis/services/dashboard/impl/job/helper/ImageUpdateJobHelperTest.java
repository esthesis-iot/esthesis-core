package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.dto.config.DashboardItemImageConfiguration;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static esthesis.core.common.AppConstants.Dashboard.Type.IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class ImageUpdateJobHelperTest {

	@Inject
	ImageUpdateJobHelper imageUpdateJobHelper;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SecuritySystemResource securitySystemResource;

	@Inject
	TestHelper testHelper;

	@Test
	void refresh() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, IMAGE);
		item.setConfiguration(createConfig());

		// Assert the image URL is set correctly.
		assertEquals("image-url.png", imageUpdateJobHelper.refresh(dashboardEntity, item).getImageUrl());
	}


	@Test
	void refreshWithError() {
		// Mock the security as not permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(false);

		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, IMAGE);

		// Assert missing configuration throws an error.
		assertTrue(imageUpdateJobHelper.refresh(dashboardEntity, item).isError());
	}


	@SneakyThrows
	String createConfig() {
		DashboardItemImageConfiguration config = new DashboardItemImageConfiguration();
		config.setHeight(100);
		config.setRefresh(1);
		config.setImageUrl("image-url.png");

		return new ObjectMapper().writeValueAsString(config);
	}

}
