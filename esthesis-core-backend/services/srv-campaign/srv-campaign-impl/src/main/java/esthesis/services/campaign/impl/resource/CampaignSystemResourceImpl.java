package esthesis.services.campaign.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.resource.CampaignSystemResource;
import esthesis.service.common.paging.Pageable;
import esthesis.services.campaign.impl.service.CampaignService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Implementation of the @{@link CampaignSystemResource}.
 */
public class CampaignSystemResourceImpl implements CampaignSystemResource {

	@Inject
	CampaignService campaignService;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public List<CampaignStatsDTO> getStats(int lastCampaigns) {

		Pageable pageable = new Pageable();
		pageable.setPage(0);
		pageable.setSize(100);
		pageable.setSort("createdOn,desc");

		return campaignService.find(pageable).getContent().stream()
			.map(campaign -> campaignService.getCampaignStats(campaign.getId().toHexString()))
			.toList();
	}

}
