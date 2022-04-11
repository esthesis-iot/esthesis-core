package esthesis.platform.backend.server.service;

import esthesis.platform.backend.server.model.Campaign;
import esthesis.platform.backend.server.repository.CampaignRepository;
import esthesis.platform.backend.server.workflow.CWFLConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Log
@Service
@Validated
@Transactional
public class WorkflowService {

  private final ProcessEngine processEngine;
  private final CampaignRepository campaignRepository;

  public WorkflowService(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") ProcessEngine processEngine,
    CampaignRepository campaignRepository) {
    this.processEngine = processEngine;
    this.campaignRepository = campaignRepository;
  }

  private Map<String, Object> prepareWorkflowVariables(Campaign campaign) {
    Map<String, Object> variables = new HashMap<>();

    // Add campaign Id.
    variables.put(CWFLConstants.VAR_CAMPAIGN_ID, campaign.getId());

    // Add campaign type and info.
    variables.put(CWFLConstants.VAR_CAMPAIGN_TYPE, campaign.getType());

    return variables;
  }

  @Async
  public void instantiate(long campaignId) {
    log.log(Level.FINEST, "Instantiating workflow for campaign Id ''{0}''.", campaignId);
    Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
    ProcessInstance processInstance = processEngine.getRuntimeService()
      .startProcessInstanceByKey(CWFLConstants.WORKFLOW_KEY, prepareWorkflowVariables(campaign));
    campaign.setProcessInstanceId(processInstance.getProcessInstanceId());
  }

  public void suspend(long campaignId) {
    log.log(Level.FINEST, "Suspending workflow for campaign Id ''{0}''.", campaignId);
    Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
    processEngine.getRuntimeService().suspendProcessInstanceById(campaign.getProcessInstanceId());
  }

  public void resume(long campaignId) {
    log.log(Level.FINEST, "Resuming workflow for campaign Id ''{0}''.", campaignId);
    Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
    processEngine.getRuntimeService().activateProcessInstanceById(campaign.getProcessInstanceId());
  }

  public void correlateMessage(long campaignId) {
    log.log(Level.FINEST, "Correlating continue message for campaign id ''{0}''", campaignId);
    Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
    processEngine.getRuntimeService()
      .createMessageCorrelation(CWFLConstants.MESSAGE_CONTINUE)
      .processInstanceId(campaign.getProcessInstanceId())
      .correlateWithResult();
  }

  public void delete(String processInstanceId) {
    log.log(Level.FINEST, "Deleting workflow for workflow instance Id ''{0}''.", processInstanceId);
    processEngine.getRuntimeService().suspendProcessInstanceById(processInstanceId);
    processEngine.getRuntimeService().deleteProcessInstance(processInstanceId, "Deleted by user");
  }
}
