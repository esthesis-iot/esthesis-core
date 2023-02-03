package esthesis.services.campaign.impl.worker;

import esthesis.service.campaign.dto.GroupDTO;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class CheckRemainingDevicesWorker extends BaseWorker {

  @Inject
  CampaignDeviceMonitorService campaignDeviceMonitorService;

  public boolean checkRemainingDevices(String campaignId, String groupExpression) {
    log.debug("checkRemainingDevice, campaignId: {}, group: {}", campaignId,
        groupExpression);
    setStateDescription(campaignId, "Checking remaining devices condition.");
    GroupDTO groupDTO = new GroupDTO(groupExpression);
    return campaignDeviceMonitorService.hasUncontactedDevices(campaignId, groupDTO.getGroup());
  }
}
