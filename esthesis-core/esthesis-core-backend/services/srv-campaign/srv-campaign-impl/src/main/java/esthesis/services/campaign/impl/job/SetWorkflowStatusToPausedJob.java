package esthesis.services.campaign.impl.job;

import esthesis.common.AppConstants.Campaign.State;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SetWorkflowStatusToPausedJob implements JobHandler {

	@Inject
	CampaignService campaignService;

	@JobWorker(type = "SetWorkflowStatusToPausedJob")
	public void handle(JobClient client, ActivatedJob job) {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
		CampaignEntity campaignEntity = campaignService.findById(p.getCampaignId());
		campaignEntity.setState(State.PAUSED_BY_WORKFLOW);
		String msg = "Campaign paused, needs to be manually resumed.";
		campaignEntity = campaignEntity.setStateDescription(msg);
		campaignService.save(campaignEntity);
		client.newCompleteCommand(job.getKey()).send().join();
	}

}
