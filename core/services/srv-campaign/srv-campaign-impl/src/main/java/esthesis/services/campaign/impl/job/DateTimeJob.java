package esthesis.services.campaign.impl.job;

import esthesis.common.AppConstants.Campaign.Condition.Op;
import esthesis.common.AppConstants.Campaign.Condition.Type;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.dto.GroupDTO;
import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;
import java.text.ChoiceFormat;
import java.time.Instant;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
@ApplicationScoped
@ZeebeWorker(type = "DateTimeJob")
public class DateTimeJob implements JobHandler {

  @Inject
  CampaignService campaignService;

  public void handle(JobClient client, ActivatedJob job) {
    WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
    GroupDTO groupDTO = new GroupDTO(job);
    log.debug("dateTimeCondition, campaignId: {}, group: {}", p.getCampaignId(), groupDTO);
    CampaignEntity campaignEntity = campaignService.setStateDescription(p.getCampaignId(),
        "Checking date/time condition.");
    List<CampaignConditionDTO> conditions = campaignService.getCondition(campaignEntity, groupDTO,
        Type.DATETIME);
    if (CollectionUtils.isEmpty(conditions)) {
      log.debug("No date/time condition found for campaign id '{}', group '{}'.", p.getCampaignId(),
          groupDTO);
      p.setDateTimeCondition(true);
    } else {
      log.debug("Found '{}' date/time {}.", conditions.size(),
          new ChoiceFormat("0#conditions|1#condition|1<conditions").format(conditions.size()));
      boolean dateTimeCondition = true;
      for (CampaignConditionDTO condition : conditions) {
        log.debug("Checking date/time condition '{}'.", condition);
        if (condition.getOperation() == Op.BEFORE) {
          if (!Instant.now().isBefore(condition.getScheduleDate())) {
            p.setDateTimeCondition(false);
          }
        } else if (condition.getOperation() == Op.AFTER) {
          if (!Instant.now().isAfter(condition.getScheduleDate())) {
            p.setDateTimeCondition(false);
          }
        } else {
          log.warn("Unsupported date/time condition operation '{}', will be skipped.",
              condition.getOperation());
        }

        if (!dateTimeCondition) {
          log.debug("Date/time condition evaluation failed, not all devices satisfy condition "
                  + "'{}'.",
              condition);
          break;
        }
      }
    }

    client.newCompleteCommand(job.getKey()).variables(p).send().join();
  }

}
