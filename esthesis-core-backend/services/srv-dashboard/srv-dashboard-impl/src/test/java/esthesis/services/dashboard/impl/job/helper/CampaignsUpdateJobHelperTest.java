package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.resource.CampaignSystemResource;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateCampaigns;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@QuarkusTest
class CampaignsUpdateJobHelperTest {

	@Inject
	CampaignsUpdateJobHelper campaignUpdateJobHelper;

	@Inject
	TestHelper testHelper;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	CampaignSystemResource campaignSystemResource;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SecuritySystemResource securitySystemResource;


	@Test
	void refreshWithSecurityError() {
		// Mock the security as not permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(false);

		// Arrange a dashboard and a Campaign item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-campaign-dashboard");
		DashboardItemDTO item =
			testHelper.makeDashboardItem(
					"test-campaign-item", 0, AppConstants.Dashboard.Type.CAMPAIGNS);


		// Assert that the security check fails.
		assertTrue(campaignUpdateJobHelper.refresh(dashboardEntity, item).isSecurityError());
	}

	@Test
	void refresh() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock the campaign stats response.
		CampaignStatsDTO running = new CampaignStatsDTO().setState(AppConstants.Campaign.State.RUNNING);
		CampaignStatsDTO pausedByUser = new CampaignStatsDTO().setState(AppConstants.Campaign.State.PAUSED_BY_USER);
		CampaignStatsDTO pausedByWorkflow = new CampaignStatsDTO().setState(AppConstants.Campaign.State.PAUSED_BY_WORKFLOW);
		CampaignStatsDTO terminatedByUser = new CampaignStatsDTO().setState(AppConstants.Campaign.State.TERMINATED_BY_USER);
		CampaignStatsDTO terminatedByWorkflow = new CampaignStatsDTO().setState(AppConstants.Campaign.State.TERMINATED_BY_WORKFLOW);

		when(campaignSystemResource.getStats(anyInt()))
			.thenReturn(List.of(running, pausedByUser, pausedByWorkflow, terminatedByUser, terminatedByWorkflow));

		// Arrange a dashboard and a CAMPAIGNS item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-campaign-dashboard");
		DashboardItemDTO item =
			testHelper.makeDashboardItem("test-campaign-item", 0, AppConstants.Dashboard.Type.CAMPAIGNS);

		// Assert the campaign item was updated correctly.
		DashboardUpdateCampaigns dashboardUpdateCampaign = campaignUpdateJobHelper.refresh(dashboardEntity, item);

		assertEquals(1, dashboardUpdateCampaign.getRunning());
		assertEquals(2, dashboardUpdateCampaign.getPaused());
		assertEquals(2, dashboardUpdateCampaign.getTerminated());


	}

	@Test
	void refreshWithError() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Arrange a dashboard and a Campaign item without required configuration.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-campaign-dashboard");
		DashboardItemDTO item =
			testHelper.makeDashboardItem("test-campaign-item", 0, AppConstants.Dashboard.Type.CAMPAIGNS)
				.setConfiguration(null);

		// Assert that the refresh method results in an error.
		assertTrue(campaignUpdateJobHelper.refresh(dashboardEntity, item).isError());
	}
}
