package esthesis.platform.server.workflow;

import esthesis.platform.server.config.AppConstants;
import esthesis.platform.server.config.AppConstants.DigitalTwins;
import esthesis.platform.server.dto.CampaignConditionDTO;
import esthesis.platform.server.service.CampaignService;
import esthesis.platform.server.service.DTService;
import lombok.extern.java.Log;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

/**
 * Checks that all devices in a group satisfy a specific property. Property checks are performed
 * using the Digital Twin API.
 */
@Log
@Component
public class PropertyConditionTask implements JavaDelegate {

  private final DTService dtService;
  private final CampaignService campaignService;


  public PropertyConditionTask(DTService dtService, CampaignService campaignService) {
    this.dtService = dtService;
    this.campaignService = campaignService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.log(Level.FINEST, "Executing PropertyConditionTask.");
    long campaignId = ConditionsHelper.getCampaignId(execution);
    String tokenLocation = ConditionsHelper.getTokenLocation(execution);
    log
      .log(Level.FINEST, "Campaign Id ''{0}'', Token at ''{1}''.",
        new Object[]{campaignId, tokenLocation});

    final List<CampaignConditionDTO> conditions = campaignService
      .findConditions(campaignId, tokenLocation, AppConstants.Campaign.Condition.Type.PROPERTY);
    log.log(Level.FINEST, "Found ''{0}'' conditions to evaluate.",
      ListUtils.emptyIfNull(conditions).size());

    int target = ConditionsHelper.getTokenTarget(execution);
    List<String> hardwareIds;
    if (target == 0) {
      hardwareIds = campaignService.findHardwareIdsForCampaign(campaignId);
    } else {
      hardwareIds = campaignService.findHardwareIdsForGroup(campaignId, target);
    }

    boolean propertyCheck = true;
    for (CampaignConditionDTO condition : conditions) {
      System.out.println(condition.getPropertyName());
      String type = condition.getPropertyName().split("\\.")[0];
      String measurement = condition.getPropertyName().split("\\.")[1];
      String field = condition.getPropertyName().split("\\.")[2];
      for (String hardwareId : hardwareIds) {
        log.log(Level.FINEST,
          "Checking property ''{0}.{1}.{2}'' for device with hardware Id ''{3}''.", new Object[]{
            type, measurement, field, hardwareId});
        String reply = dtService.extractMetadataOrTelemetrySingleValue(DigitalTwins.Type.valueOf(type),
          hardwareId, AppConstants.DigitalTwins.DTOperations.QUERY, measurement, field, 0L,
          Instant.now().toEpochMilli(), 1, 1);
        log.log(Level.FINEST, "Property value: {0}.", reply);
        // If the value given for this property is numeric, perform a numeric comparison.
        // Otherwise, perform a String comparison.
        if (StringUtils.isNumeric(condition.getValue())) {
          switch (condition.getOperation()) {
            case (AppConstants.Campaign.Condition.Op.LT) -> propertyCheck =
              propertyCheck && Double.parseDouble(reply) < Double.parseDouble(condition.getValue());
            case (AppConstants.Campaign.Condition.Op.LTE) -> propertyCheck =
              propertyCheck && Double.parseDouble(reply) <= Double
                .parseDouble(condition.getValue());
            case (AppConstants.Campaign.Condition.Op.EQUAL) -> propertyCheck =
              propertyCheck && Double.parseDouble(reply) == Double
                .parseDouble(condition.getValue());
            case (AppConstants.Campaign.Condition.Op.GT) -> propertyCheck =
              propertyCheck && Double.parseDouble(reply) > Double.parseDouble(condition.getValue());
            case (AppConstants.Campaign.Condition.Op.GTE) -> propertyCheck =
              propertyCheck && Double.parseDouble(reply) >= Double
                .parseDouble(condition.getValue());
          }
        } else {
          int comparisonResult = reply.compareTo(condition.getValue());
          switch (condition.getOperation()) {
            case (AppConstants.Campaign.Condition.Op.LT), (AppConstants.Campaign.Condition.Op.LTE) -> propertyCheck =
              propertyCheck && comparisonResult == -1;
            case (AppConstants.Campaign.Condition.Op.GT), (AppConstants.Campaign.Condition.Op.GTE) -> propertyCheck =
              propertyCheck && comparisonResult == 1;
            case (AppConstants.Campaign.Condition.Op.EQUAL) -> propertyCheck =
              propertyCheck && comparisonResult == 0;
          }
        }
        if (!propertyCheck) {
          break;
        }
      }
      if (!propertyCheck) {
        break;
      }
    }

    execution.setVariable("propertyOK", propertyCheck);
  }
}
