package esthesis.platform.backend.server.workflow;

import esthesis.platform.backend.server.config.AppConstants;
import esthesis.platform.backend.server.config.AppConstants.Campaign.Condition.Type;
import esthesis.platform.backend.server.dto.CampaignConditionDTO;
import esthesis.platform.backend.server.service.CampaignService;
import lombok.extern.java.Log;
import org.apache.commons.collections4.ListUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

/**
 * Checks if the workflow can continue based on a Date/Time condition.
 */
@Log
@Component
public class DateTimeConditionTask implements JavaDelegate {

  private final CampaignService campaignService;

  public DateTimeConditionTask(
    CampaignService campaignService) {
    this.campaignService = campaignService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.log(Level.FINEST, "Executing DateTimeConditionTask.");
    long campaignId = ConditionsHelper.getCampaignId(execution);
    String tokenLocation = ConditionsHelper.getTokenLocation(execution);
    log
      .log(Level.FINEST, "Campaign Id ''{0}'', Token at ''{1}''.",
        new Object[]{campaignId, tokenLocation});

    final List<CampaignConditionDTO> conditions = campaignService
      .findConditions(campaignId, tokenLocation, Type.DATETIME);
    log.log(Level.FINEST, "Found ''{0}'' conditions to evaluate.",
      ListUtils.emptyIfNull(conditions).size());

    boolean dateCheck = true;
    for (CampaignConditionDTO campaignConditionDTO : conditions) {
      System.out.println(campaignConditionDTO.getScheduleDate());
      switch (campaignConditionDTO.getOperation()) {
        case AppConstants.Campaign.Condition.Op.BEFORE: {
          dateCheck = dateCheck && Instant.now().isBefore(campaignConditionDTO.getScheduleDate());
        }
        case AppConstants.Campaign.Condition.Op.AFTER: {
          dateCheck = dateCheck && Instant.now().isAfter(campaignConditionDTO.getScheduleDate());
        }
      }
      if (!dateCheck) {
        break;
      }
    }

    log.log(Level.FINEST, "Date/Time check evaluated to ''{0}''.", dateCheck);
    execution.setVariable("datetimeOK", dateCheck);
  }
}
