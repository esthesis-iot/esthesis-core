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

import java.util.List;

/**
 * Find groups job handler.
 */
@Slf4j
@ApplicationScoped
public class FindGroupsJob implements JobHandler {

	@Inject
	@RestClient
	CampaignSystemResource campaignSystemResource;

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
		campaignSystemResource.setStateDescription(p.getCampaignId(), "Finding groups.");
		List<Integer> groups = campaignSystemResource.findGroups(p.getCampaignId());
		log.debug("Found groups '{}'.", groups);

		p.setGroups(groups);
		client.newCompleteCommand(job.getKey()).variables(p).send().join();
	}

}
