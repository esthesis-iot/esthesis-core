package esthesis.services.campaign.impl.job;

import esthesis.core.common.AppConstants.Campaign.Condition.Type;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.dto.GroupDTO;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * A job handler that checks the rate condition for a campaign.
 */
@Slf4j
@ApplicationScoped
public class CheckRateJob implements JobHandler {

	@Inject
	CampaignService campaignService;

	@Inject
	CampaignDeviceMonitorService campaignDeviceMonitorService;

	/**
	 * Find a rate condition set for a group in a campaign.
	 *
	 * @param client the job client to use.
	 * @param job    the job to handle.
	 * @throws Exception if an error occurs.
	 */
	@Override
	@JobWorker(type = "CheckRateJob")
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
		GroupDTO groupDTO = new GroupDTO(job);
		boolean rateCondition;

		log.debug("rateCondition, campaignId: {}, group: {}", p.getCampaignId(), groupDTO);
		CampaignEntity campaignEntity = campaignService.setStateDescription(p.getCampaignId(),
			"Checking rate condition.");

		// Get the requested rate number.
		List<CampaignConditionDTO> conditions = campaignService.getCondition(
			campaignEntity, groupDTO, Type.SUCCESS);
		if (CollectionUtils.isEmpty(conditions)) {
			log.debug("No rate condition found for campaign id '{}', group '{}'.", p.getCampaignId(),
				groupDTO);
			rateCondition = true;
		} else {
			if (conditions.size() > 1) {
				log.warn(
					"Found '{}' rate conditions for campaign id '{}', group '{}', using the first one.",
					conditions.size(), p.getCampaignId(), groupDTO);
			} else {
				log.debug("Found batch condition '{}' for campaign id '{}', group '{}'.",
					conditions.getFirst(), p.getCampaignId(), groupDTO);
			}
			BigDecimal requestedRate = new BigDecimal(conditions.getFirst().getValue());
			requestedRate = requestedRate.divide(new BigDecimal(100), 2, RoundingMode.FLOOR);
			BigDecimal actualRate = campaignDeviceMonitorService.checkRate(p.getCampaignId(),
				groupDTO.getGroup());
			rateCondition = actualRate.compareTo(requestedRate) >= 0;
			log.debug("Requested rate: '{}', actual rate: '{}'.", requestedRate, actualRate);
		}

		p.setRateCondition(rateCondition);
		client.newCompleteCommand(job.getKey()).variables(p).send().join();
	}
}
