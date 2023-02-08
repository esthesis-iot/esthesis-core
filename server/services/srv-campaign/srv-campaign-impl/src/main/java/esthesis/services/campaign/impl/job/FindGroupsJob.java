package esthesis.services.campaign.impl.job;

import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@ZeebeWorker(type = "FindGroupsJob")
public class FindGroupsJob implements JobHandler {

  @Inject
  CampaignService campaignService;

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
