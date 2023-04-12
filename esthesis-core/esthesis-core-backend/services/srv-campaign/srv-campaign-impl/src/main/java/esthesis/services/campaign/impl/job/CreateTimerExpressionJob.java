package esthesis.services.campaign.impl.job;

import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@ZeebeWorker(type = "CreateTimerExpressionJob")
public class CreateTimerExpressionJob implements JobHandler {

  @Inject
  CampaignService campaignService;

  public void handle(JobClient client, ActivatedJob job) {
    WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
    String timerExpression = "PT" + p.getMinutes() + "M";
    log.info("Setting timer expression to '{}'.", timerExpression);
    p.setTimerExpression(timerExpression);
    campaignService.setStateDescription(p.getCampaignId(), "Campaign paused by workflow, will "
        + "automatically resume in " + p.getMinutes() + " minute(s).");
    client.newCompleteCommand(job.getKey()).variables(p).send().join();
  }

}
