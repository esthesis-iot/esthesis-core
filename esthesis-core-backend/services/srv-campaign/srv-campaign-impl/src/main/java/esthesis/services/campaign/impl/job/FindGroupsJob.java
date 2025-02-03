package esthesis.services.campaign.impl.job;

import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Find groups job handler.
 */
@Slf4j
@ApplicationScoped
public class FindGroupsJob implements JobHandler {

	@Inject
	CampaignService campaignService;

	/**
	 * Finds the available groups for a campaign.
	 *
	 * @param client the Job client to use.
	 * @param job    the job to handle.
	 */
	@JobWorker(type = "FindGroupsJob")
	public void handle(JobClient client, ActivatedJob job) {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);

		log.debug("Finding groups for campaign id '{}'.", p.getCampaignId());
		campaignService.setStateDescription(p.getCampaignId(), "Finding groups.");
		List<Integer> groups = campaignService.findGroups(p.getCampaignId());
		log.debug("Found groups '{}'.", groups);

		p.setGroups(groups);
		client.newCompleteCommand(job.getKey()).variables(p).send().join();
	}

}
