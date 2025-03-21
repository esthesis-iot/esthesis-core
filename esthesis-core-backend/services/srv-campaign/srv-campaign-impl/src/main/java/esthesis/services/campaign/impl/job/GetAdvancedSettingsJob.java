package esthesis.services.campaign.impl.job;

import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Advanced settings job handler.
 */
@Slf4j
@ApplicationScoped
public class GetAdvancedSettingsJob implements JobHandler {

	@Inject
	CampaignService campaignService;

	/**
	 * Retrieves advanced settings for a campaign.
	 *
	 * @param client the job client to use.
	 * @param job    the job to handle.
	 */
	@Override
	@JobWorker(type = "GetAdvancedSettingsJob")
	public void handle(JobClient client, ActivatedJob job) {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
		CampaignEntity campaign = campaignService.findById(p.getCampaignId());

		log.debug("Setting advanced advancedDateTimeRecheckTimer to  setting GetAdvancedSettingsJob: "
				+ "campaignId: {}, campaign: {}",
			p.getCampaignId(), campaign);
		p.setAdvancedDateTimeRecheckTimer(campaign.getAdvancedDateTimeRecheckTimer());
		p.setAdvancedPropertyRecheckTimer(campaign.getAdvancedPropertyRecheckTimer());
		p.setAdvancedUpdateRepliesTimer(campaign.getAdvancedUpdateRepliesTimer());
		p.setAdvancedUpdateRepliesFinalTimer(campaign.getAdvancedUpdateRepliesFinalTimer());

		client.newCompleteCommand(job.getKey()).variables(p).send().join();
	}
}
