package esthesis.platform.backend.server.workflow;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

@Log
@Component
public class EndTask implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long campaignId = ConditionsHelper.getCampaignId(execution);
    log.log(Level.FINEST,"Workflow for campaign Id ''{0}'' terminated successfully.", campaignId);
  }
}
