package esthesis.platform.server.workflow;

import esthesis.platform.server.service.CampaignService;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

/**
 * Finds the groups defined on a campaign, so to loop over them.
 */
@Log
@Component
public class FindGroupsTask implements JavaDelegate {

  private final CampaignService campaignService;

  public FindGroupsTask(CampaignService campaignService) {
    this.campaignService = campaignService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long campaignId = ConditionsHelper.getCampaignId(execution);
    int campaignGroups = campaignService.countCampaignGroups(campaignId);
    log.log(Level.FINEST, "Campaign Id ''{0}'' has ''{1}'' group(s).",
      new Object[]{campaignId, campaignGroups});
    execution.setVariable("groups", Variables.integerValue(campaignGroups));
  }
}
