package esthesis.services.campaign.impl.job;

import esthesis.core.common.AppConstants.Campaign.Condition.Op;
import esthesis.core.common.AppConstants.Campaign.Condition.Type;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.resource.CampaignSystemResource;
import esthesis.services.campaign.impl.dto.GroupDTO;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.text.ChoiceFormat;
import java.time.Instant;
import java.util.List;

/**
 * Date/time condition job handler.
 */
@Slf4j
@ApplicationScoped
public class DateTimeJob implements JobHandler {

	@Inject
	@RestClient
	CampaignSystemResource campaignSystemResource;

	/**
	 * Handle date/time condition job, optionally pausing the campaign until the date/time condition
	 * is met.
	 *
	 * @param client the job client to use.
	 * @param job    the job to handle.
	 */
	@JobWorker(type = "DateTimeJob")
	public void handle(JobClient client, ActivatedJob job) {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
		GroupDTO groupDTO = new GroupDTO(job);
		log.debug("dateTimeCondition, campaignId: {}, group: {}", p.getCampaignId(), groupDTO);
		CampaignEntity campaignEntity = campaignSystemResource.setStateDescription(p.getCampaignId(),
			"Checking date/time condition.");
		List<CampaignConditionDTO> conditions = campaignSystemResource.getCondition(campaignEntity.getId().toHexString(),
			groupDTO.getGroup(), groupDTO.getStage(), Type.DATETIME);
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
						dateTimeCondition = false;
					}
				} else if (condition.getOperation() == Op.AFTER) {
					if (!Instant.now().isAfter(condition.getScheduleDate())) {
						p.setDateTimeCondition(false);
						dateTimeCondition = false;
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
			p.setDateTimeCondition(dateTimeCondition);
		}

		client.newCompleteCommand(job.getKey()).variables(p).send().join();
	}

}
