package esthesis.services.campaign.impl.job;

import esthesis.core.common.AppConstants.Campaign.State;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class TerminateCampaignJob implements JobHandler {

	@Inject
	CampaignService campaignService;

	@JobWorker(type = "TerminateCampaignJob")
	public void handle(JobClient client, ActivatedJob job) {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
		log.debug("terminateCampaign, campaignId: {}", p.getCampaignId());
		CampaignEntity campaignEntity = campaignService.setStateDescription(p.getCampaignId(),
			"Terminating campaign.");
		campaignEntity.setState(State.TERMINATED_BY_WORKFLOW);
		campaignEntity.setTerminatedOn(Instant.now());
		campaignEntity.setStateDescription("Campaign terminated at " + Instant.now() + ".");
		campaignService.saveUpdate(campaignEntity);
		log.debug("Campaign with id '{}' terminated.", p.getCampaignId());

		client.newCompleteCommand(job.getKey()).send().join();
	}

}
