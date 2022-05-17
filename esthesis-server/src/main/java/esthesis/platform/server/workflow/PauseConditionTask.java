package esthesis.platform.server.workflow;

import static esthesis.platform.server.workflow.CWFLConstants.VAR_TIMER;

import esthesis.platform.server.config.AppConstants.Campaign.Condition.Op;
import esthesis.platform.server.config.AppConstants.Campaign.Condition.Type;
import esthesis.platform.server.dto.CampaignConditionDTO;
import esthesis.platform.server.service.CampaignService;
import java.util.List;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.apache.commons.collections4.ListUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;

/**
 * Evaluates a pause condition, to decide whether the worfklow should wait for a specific amount of
 * time or indefinitely for user input.
 */
@Log
@Component
public class PauseConditionTask implements JavaDelegate {

  private final CampaignService campaignService;

  public PauseConditionTask(
    CampaignService campaignService) {
    this.campaignService = campaignService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.log(Level.FINEST, "Executing PauseConditionTask.");
    long campaignId = ConditionsHelper.getCampaignId(execution);
    String tokenLocation = ConditionsHelper.getTokenLocation(execution);
    log
      .log(Level.FINEST, "Campaign Id ''{0}'', Token at ''{1}''.",
        new Object[]{campaignId, tokenLocation});

    final List<CampaignConditionDTO> conditions = campaignService
      .findConditions(campaignId, tokenLocation, Type.PAUSE);
    log.log(Level.FINEST, "Found ''{0}'' conditions to evaluate.",
      ListUtils.emptyIfNull(conditions).size());

    int timerValue = -1;
    if (!conditions.isEmpty()) {
      CampaignConditionDTO campaignConditionDTO = conditions.get(0);
      if (campaignConditionDTO.getOperation() == Op.FOREVER) {
        timerValue = 0;
      } else if (campaignConditionDTO.getOperation() == Op.TIMER_MINUTES) {
        timerValue = Integer.parseInt(campaignConditionDTO.getValue());
      }
    }
    log.log(Level.FINEST, "Setting ''{0}'' to ''{1}''.", new Object[]{VAR_TIMER, timerValue});
    execution.setVariable(VAR_TIMER, Variables.integerValue(timerValue));
  }
}
