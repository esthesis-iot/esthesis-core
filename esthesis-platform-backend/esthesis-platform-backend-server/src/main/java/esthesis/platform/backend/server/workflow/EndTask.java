package esthesis.platform.backend.server.workflow;

import esthesis.platform.backend.server.service.CampaignService;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

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
    campaignService.terminateCampaignByWorkflow(campaignId);
    campaignService.updateDeviceReplies(campaignId);
    log.log(Level.FINEST, "Workflow for campaign Id ''{0}'' terminated successfully.", campaignId);
  }
}
