package esthesis.platform.backend.server.workflow;

import esthesis.platform.backend.server.config.AppConstants.Campaign.Condition.Type;
import esthesis.platform.backend.server.dto.CampaignConditionDTO;
import esthesis.platform.backend.server.repository.CampaignDeviceMonitorRepository;
import esthesis.platform.backend.server.repository.CommandReplyRepository;
import esthesis.platform.backend.server.service.CampaignService;
import java.util.List;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;

/**
 * Checks if the success rate of a campaign is satisfied before allowing the workflow to proceed.
 */
@Log
@Component
public class CheckRateTask implements JavaDelegate {
  private final CampaignService campaignService;
  private final CampaignDeviceMonitorRepository campaignDeviceMonitorRepository;
  private final CommandReplyRepository commandReplyRepository;

  public CheckRateTask(CampaignService campaignService,
    CampaignDeviceMonitorRepository campaignDeviceMonitorRepository,
    CommandReplyRepository commandReplyRepository) {
    this.campaignService = campaignService;
    this.campaignDeviceMonitorRepository = campaignDeviceMonitorRepository;
    this.commandReplyRepository = commandReplyRepository;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.log(Level.FINEST, "Executing CheckRateTask.");

    // Extract campaign details.
    long campaignId = ConditionsHelper.getCampaignId(execution);
    String tokenLocation = ConditionsHelper.getTokenLocation(execution);
    int groupOrder =
      Integer.parseInt(tokenLocation.substring(tokenLocation.lastIndexOf("_") + 1)) + 1;
    // Update device monitor with devices that have already replied.
    campaignService.updateDeviceReplies(campaignId);

    // Check rate.
    final List<CampaignConditionDTO> conditions = campaignService
      .findConditions(campaignId, tokenLocation, Type.SUCCESS);
    log.log(Level.FINEST, "Found ''{0}'' conditions to evaluate.", ListUtils.emptyIfNull(conditions).size());
    if (!CollectionUtils.isEmpty(conditions)) {
      CampaignConditionDTO condition = conditions.get(0);
      if (condition.getValue().endsWith("%")) {
        int conditionNumber = Integer.parseInt(condition.getValue().substring(0,
          condition.getValue().length() -1));
        execution.setVariable(CWFLConstants.VAR_RATE_OK,Variables.booleanValue(
          campaignDeviceMonitorRepository.findRateAsPercentage(campaignId, groupOrder) >= conditionNumber));
      } else {
        execution.setVariable(CWFLConstants.VAR_RATE_OK,Variables.booleanValue(
          campaignDeviceMonitorRepository.findRateAsNumber(campaignId, groupOrder) >= Integer.parseInt(condition.getValue())
        ));
      }
    } else {
      execution.setVariable(CWFLConstants.VAR_RATE_OK,Variables.booleanValue(true));
    }
    log.log(Level.FINEST, "Setting campaign Id ''{0}'', group ''{1}'', Rate condition to ''{2}''"
      + ".", new Object[]{campaignId, groupOrder, execution.getVariable(CWFLConstants.VAR_RATE_OK)});
  }
}
