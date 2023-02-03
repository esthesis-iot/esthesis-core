package esthesis.services.campaign.impl.worker;

import esthesis.common.AppConstants.Campaign.Condition.Stage;
import esthesis.common.AppConstants.Campaign.State;
import esthesis.service.campaign.dto.GroupDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.service.CampaignService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class StatusWorker extends BaseWorker {

  @Inject
  CampaignService campaignService;

  public void setStatusToPaused(String campaignId, String group) {
    setStateDescription(campaignId, "Setting status to paused.");
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    campaignEntity.setState(State.PAUSED_BY_WORKFLOW);

    String entryExitMsg;
    GroupDTO groupDTO = new GroupDTO(group);
    if (groupDTO.getStage() == Stage.ENTRY) {
      entryExitMsg = "entering";
    } else {
      entryExitMsg = "exiting";
    }
    String groupNo;
    if (groupDTO.getGroup() == 0) {
      groupNo = "global group";
    } else {
      groupNo = "group " + groupDTO.getGroup();
    }
    String msg = "Campaign paused while " + entryExitMsg + " " + groupNo
        + ", needs to be manually resumed.";
    log.debug("Setting state description campaign id '{}' to '{}'", campaignId, msg);
    campaignEntity.setStateDescription(msg);
    campaignService.save(campaignEntity);
  }
}
