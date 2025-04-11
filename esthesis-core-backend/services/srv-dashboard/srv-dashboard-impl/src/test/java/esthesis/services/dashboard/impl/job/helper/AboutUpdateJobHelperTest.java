package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.about.resource.AboutSystemResource;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateAbout;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class AboutUpdateJobHelperTest {

	@Inject
	AboutUpdateJobHelper aboutUpdateJobHelper;

	@Inject
	TestHelper testHelper;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	AboutSystemResource aboutSystemResource;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SecuritySystemResource securitySystemResource;


	@Test
	void refreshWithSecurityError() {
		// Mock the security as not permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(false);

		// Arrange a dashboard and an ABOUT item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-about-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-about-item", 0, Type.ABOUT);

		// Assert that the security check fails.
		assertTrue(aboutUpdateJobHelper.refresh(dashboardEntity, item).isSecurityError());
	}

	@Test
	void refresh() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock the GeneralInfo response.
		AboutGeneralDTO generalInfo = new AboutGeneralDTO();
		generalInfo.setGitBuildTime("2025-04-09T00:00:00Z");
		generalInfo.setGitCommitId("test-id");
		generalInfo.setGitVersion("test-version");
		generalInfo.setGitCommitIdAbbrev("test-abbrev");
		when(aboutSystemResource.getGeneralInfo()).thenReturn(generalInfo);

		// Arrange a dashboard and an ABOUT item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-about-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-about-item", 0, Type.ABOUT);

		// Assert the about item is updated correctly.
		DashboardUpdateAbout dashboardUpdateAbout = aboutUpdateJobHelper.refresh(dashboardEntity, item);
		assertEquals("test-id", dashboardUpdateAbout.getGitCommitId());
		assertEquals("test-version", dashboardUpdateAbout.getGitVersion());
		assertEquals("2025-04-09T00:00:00Z", dashboardUpdateAbout.getGitBuildTime());
		assertEquals("test-abbrev", dashboardUpdateAbout.getGitCommitIdAbbrev());


	}

	@Test
	void refreshWithError() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock exception during general info retrieval.
		when(aboutSystemResource.getGeneralInfo()).thenThrow(new RuntimeException("Test exception"));

		// Arrange a dashboard and an ABOUT item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-about-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-about-item", 0, Type.ABOUT);

		// Assert that the refresh method results in an error.
		assertTrue(aboutUpdateJobHelper.refresh(dashboardEntity, item).isError());



	}
}
