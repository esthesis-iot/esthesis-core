package esthesis.services.campaign.impl.worker;

import esthesis.common.entity.CommandReplyEntity;
import esthesis.service.campaign.dto.GroupDTO;
import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.command.resource.CommandSystemResource;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class UpdateRepliesWorker extends BaseWorker {

  @Inject
  CampaignDeviceMonitorService campaignDeviceMonitorService;

  @Inject
  @RestClient
  CommandSystemResource commandSystemResource;

  public void updateReplies(String campaignId, String groupExpression) {
    setStateDescription(campaignId, "Updating replies.");
    GroupDTO groupDTO = new GroupDTO(groupExpression);
    // Before checking the rate, update any possible replies received.
    List<CampaignDeviceMonitorEntity> contactedDevices =
        campaignDeviceMonitorService.findContactedNotReplied(campaignId, groupDTO.getGroup());
    contactedDevices.forEach(device -> {
      List<CommandReplyEntity> replies = commandSystemResource.getReplies(
          device.getCommandRequestId().toString());
      if (!CollectionUtils.isEmpty(replies)) {
        device.setCommandReplyId(replies.get(0).getId());
        campaignDeviceMonitorService.save(device);
        log.debug("Updating reply '{}' for device '{}'.", replies.get(0), device.getHardwareId());
      }
    });
  }
}
