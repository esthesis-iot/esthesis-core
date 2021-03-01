package esthesis.platform.backend.server.workflow;

import esthesis.platform.backend.server.service.CampaignService;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

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
    campaignService.pauseCampaignByWorkflow(campaignId);
  }
}
