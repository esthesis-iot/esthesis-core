package esthesis.services.campaign.impl.resource;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.resource.CampaignResource;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.services.campaign.impl.service.CampaignService;
import esthesis.util.kogito.client.KogitoClient;
import javax.inject.Inject;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

public class CampaignResourceImpl implements CampaignResource {

  @Inject
  CampaignService campaignService;

  @Inject
  KogitoClient kogitoClient;

  @Override
  @Audited(cat = Category.CAMPAIGN, op = Operation.READ, msg = "Find campaigns")
  public Page<CampaignEntity> find(Pageable pageable) {
    return campaignService.find(pageable);
  }

  @Override
  @Audited(cat = Category.CAMPAIGN, op = Operation.WRITE, msg = "Save campaign")
  public void save(CampaignEntity campaignEntity) {
    campaignService.save(campaignEntity);
  }

  @Override
  @Audited(cat = Category.CAMPAIGN, op = Operation.READ, msg = "View campaign")
  public CampaignEntity findById(String campaignId) {
    return campaignService.findById(campaignId);
  }

  @Override
  @Audited(cat = Category.CAMPAIGN, op = Operation.WRITE, msg = "Start campaign")
  public void start(String campaignId) {
    campaignService.start(campaignId);
  }

  @Override
  @Audited(cat = Category.CAMPAIGN, op = Operation.WRITE, msg = "Resume campaign")
  public void resume(String campaignId) {
    campaignService.resume(campaignId);
  }

  @Override
  @Audited(cat = Category.CAMPAIGN, op = Operation.WRITE, msg = "Replicate campaign")
  public CampaignEntity replicate(String campaignId) {
    return campaignService.replicate(campaignId);
  }

  @Override
  @Audited(cat = Category.CAMPAIGN, op = Operation.WRITE, msg = "Terminate campaign")
  public void terminate(String campaignId) {
    campaignService.terminate(campaignId);
  }

  @Override
  public CampaignStatsDTO getCampaignStats(String campaignId) {
    return campaignService.getCampaignStats(campaignId);
  }

  @Audited(cat = Category.CAMPAIGN, op = Operation.DELETE, msg = "Delete campaign")
  public Response delete(@PathParam("id") String campaignId) {
    campaignService.delete(campaignId);
    return Response.ok().build();
  }

}
