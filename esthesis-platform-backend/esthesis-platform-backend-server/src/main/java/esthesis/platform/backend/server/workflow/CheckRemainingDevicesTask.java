package esthesis.platform.backend.server.workflow;

import esthesis.platform.backend.server.repository.CampaignDeviceMonitorRepository;
import esthesis.platform.backend.server.repository.CommandReplyRepository;
import esthesis.platform.backend.server.service.CampaignService;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;

@Log
@Component
public class CheckRemainingDevicesTask implements JavaDelegate {

  private final CampaignService campaignService;
  private final CampaignDeviceMonitorRepository campaignDeviceMonitorRepository;
  private final CommandReplyRepository commandReplyRepository;

  public CheckRemainingDevicesTask(
    CampaignService campaignService,
    CampaignDeviceMonitorRepository campaignDeviceMonitorRepository,
    CommandReplyRepository commandReplyRepository) {
    this.campaignService = campaignService;
    this.campaignDeviceMonitorRepository = campaignDeviceMonitorRepository;
    this.commandReplyRepository = commandReplyRepository;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.log(Level.FINEST, "Executing CheckRemainingDevicesTask.");

    // Extract campaign details.
    long campaignId = ConditionsHelper.getCampaignId(execution);
    int target = ConditionsHelper.getTokenTarget(execution);
    int batchSize = campaignService.findBatchSize(campaignId, target);

    // Check if additional devices are left for this campaign.
    execution.setVariable(CWFLConstants.VAR_HAS_MORE_DEVICES,
      Variables.booleanValue(
        campaignDeviceMonitorRepository.countByCampaignIdAndCommandRequestIdNull(campaignId) > 0));
  }
}
