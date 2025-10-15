package esthesis.services.campaign.impl.job;

import esthesis.core.common.AppConstants.Campaign.State;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.resource.CampaignSystemResource;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;

/**
 * A job handler for terminating a campaign.
 */
@Slf4j
@ApplicationScoped
public class TerminateCampaignJob implements JobHandler {

	@Inject
	@RestClient
	CampaignSystemResource campaignSystemResource;

	/**
	 * Terminates a campaign.
	 *
	 * @param client The job client.
	 * @param job    The activated job.
	 */
	@JobWorker(type = "TerminateCampaignJob")
	public void handle(JobClient client, ActivatedJob job) {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
		log.debug("terminateCampaign, campaignId: {}", p.getCampaignId());
		CampaignEntity campaignEntity = campaignSystemResource.setStateDescription(p.getCampaignId(),
			"Terminating campaign.");
		campaignEntity.setState(State.TERMINATED_BY_WORKFLOW);
		campaignEntity.setTerminatedOn(Instant.now());
		campaignEntity.setStateDescription("Campaign terminated at " + Instant.now() + ".");
		campaignSystemResource.save(campaignEntity);
		log.debug("Campaign with id '{}' terminated.", p.getCampaignId());

		client.newCompleteCommand(job.getKey()).send().join();
	}

}
