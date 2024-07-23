package esthesis.services.campaign.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.resource.CampaignResource;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.services.campaign.impl.service.CampaignService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public class CampaignResourceImpl implements CampaignResource {

	@Inject
	CampaignService campaignService;

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CAMPAIGN, op = Operation.READ, msg = "Find campaigns")
	public Page<CampaignEntity> find(Pageable pageable) {
		return campaignService.find(pageable);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CAMPAIGN, op = Operation.WRITE, msg = "Save campaign")
	public void save(CampaignEntity campaignEntity) {
		if (campaignEntity.getId() == null) {
			campaignService.saveNew(campaignEntity);
		} else {
			campaignService.saveUpdate(campaignEntity);
		}
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CAMPAIGN, op = Operation.READ, msg = "View campaign")
	public CampaignEntity findById(String campaignId) {
		return campaignService.findById(campaignId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CAMPAIGN, op = Operation.WRITE, msg = "Start campaign")
	public void start(String campaignId) {
		campaignService.start(campaignId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CAMPAIGN, op = Operation.WRITE, msg = "Resume campaign")
	public void resume(String campaignId) {
		campaignService.resume(campaignId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CAMPAIGN, op = Operation.WRITE, msg = "Replicate campaign")
	public CampaignEntity replay(String campaignId) {
		return campaignService.replicate(campaignId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CAMPAIGN, op = Operation.WRITE, msg = "Terminate campaign")
	public void terminate(String campaignId) {
		campaignService.terminate(campaignId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public CampaignStatsDTO getCampaignStats(String campaignId) {
		return campaignService.getCampaignStats(campaignId);
	}

	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CAMPAIGN, op = Operation.DELETE, msg = "Delete campaign")
	public Response delete(@PathParam("id") String campaignId) {
		campaignService.delete(campaignId);
		return Response.ok().build();
	}
}
