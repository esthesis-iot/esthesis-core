package esthesis.platform.backend.server.workflow;

import esthesis.platform.backend.common.config.AppConstants.Device.CommandType;
import esthesis.platform.backend.server.config.AppConstants.Campaign.Type;
import esthesis.platform.backend.server.model.Campaign;
import esthesis.platform.backend.server.model.CampaignDeviceMonitor;
import esthesis.platform.backend.server.repository.CampaignDeviceMonitorRepository;
import esthesis.platform.backend.server.repository.CommandRequestRepository;
import esthesis.platform.backend.server.service.CampaignService;
import esthesis.platform.backend.server.service.CommandRequestService;
import esthesis.platform.backend.server.service.DTService;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Log
@Component
public class SendCommandTask implements JavaDelegate {

  private final CampaignService campaignService;
  private final CampaignDeviceMonitorRepository campaignDeviceMonitorRepository;
  private final DTService dtService;
  private final CommandRequestRepository commandRequestRepository;
  private final CommandRequestService commandRequestService;

  public SendCommandTask(CampaignService campaignService,
    CampaignDeviceMonitorRepository campaignDeviceMonitorRepository, DTService dtService,
    CommandRequestRepository commandRequestRepository,
    CommandRequestService commandRequestService) {
    this.campaignService = campaignService;
    this.campaignDeviceMonitorRepository = campaignDeviceMonitorRepository;
    this.dtService = dtService;
    this.commandRequestRepository = commandRequestRepository;
    this.commandRequestService = commandRequestService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.log(Level.FINEST, "Executing SendCommandTask.");
    long campaignId = ConditionsHelper.getCampaignId(execution);
    String tokenLocation = ConditionsHelper.getTokenLocation(execution);
    int target = ConditionsHelper.getTokenTarget(execution);
    log.log(Level.FINEST, "Campaign Id ''{0}'', Token at ''{1}'', Target is ''{2}''.",
      new Object[]{campaignId, tokenLocation, target});

    // Find the batch size for the currently executing group.
    int batchSize = campaignService.findBatchSize(campaignId, target);

    // Find what type of command will be executed.
    Campaign campaign = campaignService.findEntityById(campaignId);

    // Find a "batchSize" number of devices belonging to this group which haven't been contacted yet.
    List<CampaignDeviceMonitor> nextBatch = campaignDeviceMonitorRepository
      .findNextBatch(campaignId, target, batchSize);
    if (nextBatch.size() > 0) {
      log.log(Level.FINEST, "About to contact device Ids: {0}",
        nextBatch.stream().map(
          cdm -> cdm.getDevice().getId() + " (hardware Id: " + cdm.getDevice().getHardwareId()
            + ").").collect(Collectors.toList()));
    } else {
      log.log(Level.FINEST, "No more devices left on this campaign to contact.");
    }
    for (CampaignDeviceMonitor cdm : nextBatch) {
      String commandRequestId = null;
      switch (campaign.getType()) {
        case (Type.COMMAND):
          commandRequestId = dtService.executeCommand(cdm.getDevice().getHardwareId(),
            CommandType.EXECUTE, "Campaign " + campaign.getName(),
            campaign.getCommandName() + " " + campaign.getCommandArguments());
          break;
        case (Type.PROVISIONING):
          break;
        case (Type.REBOOT):
          break;
        case (Type.SHUTDOWN):
          break;
      }
      cdm.setCommandRequestId(Long.parseLong(commandRequestId));
    }
  }
}
