package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import esthesis.core.common.AppConstants.Campaign.State;
import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.resource.CampaignSystemResource;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateCampaigns;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class CampaignsUpdateJobHelper extends UpdateJobHelper<DashboardUpdateCampaigns> {

	@Inject
	@RestClient
	CampaignSystemResource campaignSystemResource;

	@Override
	public DashboardUpdateCampaigns refresh(DashboardEntity dashboardEntity, DashboardItemDTO item)
	throws JsonProcessingException {
		List<CampaignStatsDTO> stats = campaignSystemResource.getStats();

		return DashboardUpdateCampaigns.builder()
			.id(item.getId())
			.type(Type.CAMPAIGNS)
			.running(stats.stream().filter(s -> s.getState() == State.RUNNING).count())
			.paused(stats.stream().filter(
					s -> s.getState() == State.PAUSED_BY_USER || s.getState() == State.PAUSED_BY_WORKFLOW)
				.count())
			.terminated(stats.stream().filter(s -> s.getState() == State.TERMINATED_BY_USER
				|| s.getState() == State.TERMINATED_BY_WORKFLOW).count())
			.build();
	}
}
