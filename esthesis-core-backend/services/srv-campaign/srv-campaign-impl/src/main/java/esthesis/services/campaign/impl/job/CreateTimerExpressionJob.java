package esthesis.services.campaign.impl.job;

import esthesis.service.campaign.resource.CampaignSystemResource;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * A job handler that creates a timer expression for a campaign, optionally pausing a campaign.
 */
@Slf4j
@ApplicationScoped
public class CreateTimerExpressionJob implements JobHandler {

	@Inject
	@RestClient
	CampaignSystemResource campaignSystemResource;

	/**
	 * Create a timer expression for a campaign.
	 *
	 * @param client the job client to use.
	 * @param job    the job to handle.
	 */
	@JobWorker(type = "CreateTimerExpressionJob")
	public void handle(JobClient client, ActivatedJob job) {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
		String timerExpression = "PT" + p.getMinutes() + "M";
		log.info("Setting timer expression to '{}'.", timerExpression);
		p.setTimerExpression(timerExpression);
		campaignSystemResource.setStateDescription(p.getCampaignId(), "Campaign paused by workflow, will "
			+ "automatically resume in " + p.getMinutes() + " minute(s).");
		client.newCompleteCommand(job.getKey()).variables(p).send().join();
	}

}
