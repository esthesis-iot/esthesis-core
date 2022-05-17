package esthesis.platform.server.workflow;

import esthesis.platform.server.config.AppConstants.Campaign.State;
import esthesis.platform.server.service.CampaignService;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * A listener to be called when the workflow enters a wait state (i.e. waiting for the user to
 * manually resume it).
 */
@Log
@Component
public class WorkflowPauseListener implements ExecutionListener {

  private final CampaignService campaignService;

  public WorkflowPauseListener(
    CampaignService campaignService) {
    this.campaignService = campaignService;
  }

  @Override
  public void notify(DelegateExecution delegateExecution) throws Exception {
    log.log(Level.FINEST, "Executing ManualPauseTask.");
    long campaignId = ConditionsHelper.getCampaignId(delegateExecution);
    campaignService.setState(campaignId, State.PAUSED_BY_WORKFLOW);
  }
}
