package esthesis.services.campaign.impl.worker;

import esthesis.services.campaign.impl.service.CampaignService;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class FindGroupsWorker extends BaseWorker {

  @Inject
  CampaignService campaignService;

  public List<Integer> findGroups(String campaignId) {
    log.debug("Finding groups for campaign id '{}'.", campaignId);
    setStateDescription(campaignId, "Finding groups.");
    List<Integer> groups = campaignService.findGroups(campaignId);
    log.debug("Found groups '{}'.", groups);

    return groups;
  }
}
