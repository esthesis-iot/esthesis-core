package esthesis.services.campaign.impl.worker;

import esthesis.common.AppConstants.Campaign.Condition.Op;
import esthesis.common.AppConstants.Campaign.Condition.Stage;
import esthesis.common.AppConstants.Campaign.Condition.Type;
import esthesis.common.AppConstants.Campaign.State;
import esthesis.common.exception.QMismatchException;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.GroupDTO;
import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import esthesis.services.campaign.impl.service.CampaignService;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import java.time.Instant;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@ApplicationScoped
public class CampaignWorker {

  @Inject
  CampaignService campaignService;

  @Inject
  CampaignDeviceMonitorService campaignDeviceMonitorService;

  @Inject
  RedisUtils redisUtils;

  private List<CampaignConditionDTO> getCondition(CampaignEntity campaignEntity, GroupDTO groupDTO,
      Type type) {
    return campaignEntity.getConditions().stream()
        .filter(condition -> condition.getType() == type)
        .filter(condition -> condition.getGroup() == groupDTO.getGroup())
        .filter(condition -> condition.getStage() == groupDTO.getStage())
        .toList();
  }

  public String createTimerExpression(int minutes) {
    return "PT" + minutes + "M";
  }

  public void setStatusToPaused(String campaignId, String group) {
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    campaignEntity.setState(State.PAUSED_BY_WORKFLOW);

    String entryExitMsg;
    GroupDTO groupDTO = new GroupDTO(group);
    if (groupDTO.getStage() == Stage.ENTRY) {
      entryExitMsg = "entering";
    } else {
      entryExitMsg = "exiting";
    }
    String groupNo;
    if (groupDTO.getGroup() == 0) {
      groupNo = "global group";
    } else {
      groupNo = "group " + groupDTO.getGroup();
    }
    String msg = "Campaign paused while " + entryExitMsg + " " + groupNo
        + ", needs to be manually resumed.";
    log.debug("Setting state description campaign id '{}' to '{}'", campaignId, msg);
    campaignEntity.setStateDescription(msg);
    campaignService.save(campaignEntity);
  }

  public int pauseCondition(String campaignId, String groupExpression) {
    log.debug("pauseCondition, campaignId: {}, group: {}", campaignId, groupExpression);
    int pauseCondition = -1;

    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    GroupDTO groupDTO = new GroupDTO(groupExpression);
    List<CampaignConditionDTO> conditions = getCondition(campaignEntity, groupDTO, Type.PAUSE);
    if (conditions.size() > 1) {
      throw new QMismatchException("More than one pause conditions found for campaign id '{}', "
          + "group '{}'.", campaignId, groupExpression);
    } else if (conditions.size() == 1) {
      log.debug("Found condition '{}'.", conditions.get(0));
      CampaignConditionDTO condition = conditions.get(0);
      if (condition.getOperation() == Op.TIMER_MINUTES) {
        pauseCondition = Integer.valueOf(condition.getValue());
      } else if (condition.getOperation() == Op.FOREVER) {
        pauseCondition = 0;
      } else {
        throw new QMismatchException("Unsupported pause condition operation '{}'.",
            condition.getOperation());
      }
    } else {
      log.debug("No pause condition found");
    }

    return pauseCondition;
  }

  public boolean dateTimeCondition(String campaignId, String groupExpression) {
    log.debug("dateTimeCondition, campaignId: {}, group: {}", campaignId, groupExpression);
    return true;
  }

  public boolean propertyCondition(String campaignId, String groupExpression) {
    log.debug("propertyCondition, campaignId: {}, group: {}", campaignId, groupExpression);
    boolean propertyCondition = true;

    // Get the campaign details, conditions, and devices for this campaign.
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    GroupDTO groupDTO = new GroupDTO(groupExpression);
    List<CampaignConditionDTO> conditions = getCondition(campaignEntity, groupDTO, Type.PROPERTY);
    if (conditions.isEmpty()) {
      return true;
    }
    List<CampaignDeviceMonitorEntity> devices = campaignDeviceMonitorService.findByCampaignID(
        campaignId);

    for (CampaignConditionDTO condition : conditions) {
      for (CampaignDeviceMonitorEntity device : devices) {
        // Get the property value of this condition from the device.
        String deviceValue = redisUtils.getFromHash(KeyType.ESTHESIS_DM, device.getHardwareId(),
            condition.getPropertyName());
        if (StringUtils.isBlank(deviceValue) && condition.getPropertyIgnorable()) {

        }

        if (!propertyCondition) {
          break;
        }
      }
      if (!propertyCondition) {
        break;
      }
    }

    return propertyCondition;
  }

  public void test(String group) {
    System.out.println("test: " + group);
  }

  public List<Integer> findGroups(String campaignId) {
    return campaignService.findGroups(campaignId);
  }

  /**
   * Creates a "group expression" based on the currently processed group. The expression has the
   * following format: "group" + group position + stage, e.g. group:1:entry, group:2:exit, etc. The
   * global group is denoted with "0", e.g. group:0:entry.
   * <p>
   * This expression is used to allow condition-checkers to find which conditions should be checked
   * at each step of the workflow.
   *
   * @param campaignId    The id of the campaign represented by the currently executing workflow.
   * @param groupPosition The position of the group, 0 denoting the global group.
   * @param groupPhase    The phase of the group, entry or exit.
   */
  public String setGroup(String campaignId, int groupPosition, String groupPhase) {
    log.trace("setGroup, campaignId: {}, groupPosition: {}, groupPhase: {}", campaignId,
        groupPosition, groupPhase);
    return "group:" + groupPosition + ":" + groupPhase;
  }

  public void terminateCampaign(String campaignId) {
    log.debug("terminateCampaign, campaignId: {}", campaignId);
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    campaignEntity.setState(State.TERMINATED_BY_WORKFLOW);
    campaignEntity.setTerminatedOn(Instant.now());
    campaignEntity.setStateDescription("Campaign terminated at " + Instant.now() + ".");
    campaignService.save(campaignEntity);
  }
}
