package esthesis.services.campaign.impl.job;

import esthesis.common.AppConstants.Campaign.Condition.Type;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.dto.GroupDTO;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import esthesis.services.campaign.impl.service.CampaignService;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;
import java.text.ChoiceFormat;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@ApplicationScoped
@ZeebeWorker(type = "PropertyJob")
public class PropertyJob implements JobHandler {

  @Inject
  CampaignService campaignService;

  @Inject
  CampaignDeviceMonitorService campaignDeviceMonitorService;

  @Inject
  RedisUtils redisUtils;

  @SuppressWarnings({"java:S2200", "java:S1121"})
  public void handle(JobClient client, ActivatedJob job) {
    WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
    GroupDTO groupDTO = new GroupDTO(job);
    log.debug("propertyCondition, campaignId: {}, group: {}", p.getCampaignId(), groupDTO);
    boolean propertyCondition;
    CampaignEntity campaignEntity = campaignService.setStateDescription(p.getCampaignId(),
        "Checking property condition.");

    // Get the campaign details, conditions, and devices for this campaign.
    List<CampaignConditionDTO> conditions = campaignService.getCondition(campaignEntity, groupDTO,
        Type.PROPERTY);
    if (CollectionUtils.isEmpty(conditions)) {
      log.debug("No property condition found for campaign id '{}', group '{}'.", p.getCampaignId(),
          groupDTO);
      propertyCondition = true;
    } else {
      log.debug("Found '{}' property {}.", conditions.size(),
          new ChoiceFormat("0#conditions|1#condition|1<conditions").format(conditions.size()));

      List<CampaignDeviceMonitorEntity> devices = campaignDeviceMonitorService.findByCampaignIdAndGroup(
          p.getCampaignId(), groupDTO.getGroup());
      log.debug("Found '{}' {}.", devices.size(),
          new ChoiceFormat("0#devices|1#device|1<devices").format(devices.size()));

      propertyCondition = true;
      for (CampaignConditionDTO condition : conditions) {
        for (CampaignDeviceMonitorEntity device : devices) {
          log.debug("Checking property condition '{}' for device '{}'.", condition, device);
          // Get the property value of this condition from the device.
          String deviceValue = redisUtils.getFromHash(KeyType.ESTHESIS_DM, device.getHardwareId(),
              condition.getPropertyName());
          // If the property value is not found but the condition is ignorable, skip this check.
          // Otherwise, evaluate the condition.
          if (StringUtils.isBlank(deviceValue) && Boolean.TRUE.equals(
              condition.getPropertyIgnorable())) {
            log.debug("Property value not found but condition is ignorable, skipping this check.");
            continue;
          } else {
            if (StringUtils.isBlank(deviceValue)) {
              log.debug("Property value not found, setting condition to false.");
              propertyCondition = false;
            } else {
              // If the value given for this property is numeric, perform a numeric comparison.
              // Otherwise, perform a String comparison.
              if (StringUtils.isNumeric(condition.getValue())) {
                switch (condition.getOperation()) {
                  case LT -> propertyCondition =
                      Double.parseDouble(deviceValue) < Double.parseDouble(condition.getValue());
                  case LTE -> propertyCondition =
                      Double.parseDouble(deviceValue) <= Double.parseDouble(condition.getValue());
                  case EQUAL -> propertyCondition =
                      Double.parseDouble(deviceValue) == Double.parseDouble(condition.getValue());
                  case GT -> propertyCondition =
                      Double.parseDouble(deviceValue) > Double.parseDouble(condition.getValue());
                  case GTE -> propertyCondition =
                      Double.parseDouble(deviceValue) >= Double.parseDouble(condition.getValue());
                }
              } else {
                int comparisonResult = deviceValue.compareTo(condition.getValue());
                switch (condition.getOperation()) {
                  case LT, LTE -> propertyCondition = comparisonResult < 0;
                  case GT, GTE -> propertyCondition = comparisonResult > 0;
                  case EQUAL -> propertyCondition = comparisonResult == 0;
                }
              }
              log.debug("Property condition result: {}", propertyCondition);
            }
          }
          if (!propertyCondition) {
            log.debug("Property condition evaluation failed, not all devices satisfy condition "
                    + "'{}'.",
                condition);
            break;
          }
        }
        if (!propertyCondition) {
          log.debug("Property condition evaluation failed, not all conditions satisfied.");
          break;
        }
      }
    }
    p.setPropertyCondition(propertyCondition);
    client.newCompleteCommand(job.getKey()).variables(p).send().join();
  }
}
