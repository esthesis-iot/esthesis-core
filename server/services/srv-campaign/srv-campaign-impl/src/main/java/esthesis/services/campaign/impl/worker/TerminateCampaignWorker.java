package esthesis.services.campaign.impl.worker;

import esthesis.common.AppConstants.Campaign.State;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.service.CampaignService;
import java.time.Instant;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class TerminateCampaignWorker extends BaseWorker {

  @Inject
  CampaignService campaignService;

  public void terminateCampaign(String campaignId) {
    log.debug("terminateCampaign, campaignId: {}", campaignId);
    setStateDescription(campaignId, "Terminating campaign.");
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    campaignEntity.setState(State.TERMINATED_BY_WORKFLOW);
    campaignEntity.setTerminatedOn(Instant.now());
    campaignEntity.setStateDescription("Campaign terminated at " + Instant.now() + ".");
    campaignService.save(campaignEntity);
  }
}
