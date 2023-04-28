package esthesis.services.campaign.impl.job;

import esthesis.common.AppConstants.Campaign.Condition.Op;
import esthesis.common.AppConstants.Campaign.Condition.Type;
import esthesis.common.exception.QMismatchException;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.dto.GroupDTO;
import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class PauseJob implements JobHandler {

	@Inject
	CampaignService campaignService;

	@JobWorker(type = "PauseJob")
	public void handle(JobClient client, ActivatedJob job) {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
		GroupDTO groupDTO = new GroupDTO(job);
		log.debug("pauseCondition, campaignId: {}, group: {}", p.getCampaignId(), groupDTO);
		campaignService.setStateDescription(p.getCampaignId(), "Checking pause condition.");
		int pauseCondition = -1;

		CampaignEntity campaignEntity = campaignService.findById(p.getCampaignId());
		List<CampaignConditionDTO> conditions = campaignService.getCondition(campaignEntity, groupDTO
			, Type.PAUSE);
		if (conditions.size() > 1) {
			throw new QMismatchException("More than one pause conditions found for campaign id '{}', "
				+ "group '{}'.", p.getCampaignId(), groupDTO);
		} else if (conditions.size() == 1) {
			log.debug("Found condition '{}'.", conditions.get(0));
			CampaignConditionDTO condition = conditions.get(0);
			if (condition.getOperation() == Op.TIMER_MINUTES) {
				pauseCondition = Integer.valueOf(condition.getValue());
			} else if (condition.getOperation() == Op.FOREVER) {
				pauseCondition = 0;
			} else {
				throw new QMismatchException("Unsupported pause condition operation '{}'.",
					condition.getOperation());
			}
		} else {
			log.debug("No pause condition found for campaign id '{}', group '{}'.", p.getCampaignId(),
				groupDTO);
		}

		p.setPauseCondition(pauseCondition);
		client.newCompleteCommand(job.getKey()).variables(p).send().join();
		campaignService.save(campaignEntity);
	}
}
