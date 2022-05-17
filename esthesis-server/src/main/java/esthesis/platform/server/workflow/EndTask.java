package esthesis.platform.server.workflow;

import esthesis.platform.server.config.AppConstants.Campaign.State;
import esthesis.platform.server.service.CampaignService;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Marks a campaign as terminated.
 */
@Log
@Component
public class EndTask implements JavaDelegate {

  private final CampaignService campaignService;

  public EndTask(CampaignService campaignService) {
    this.campaignService = campaignService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long campaignId = ConditionsHelper.getCampaignId(execution);
    campaignService.setState(campaignId, State.TERMINATED_BY_WORKFLOW);
    campaignService.updateDeviceReplies(campaignId);
    log.log(Level.FINEST, "Workflow for campaign Id ''{0}'' terminated successfully.", campaignId);
  }
}
