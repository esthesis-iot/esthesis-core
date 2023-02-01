package esthesis.services.campaign.impl.worker;

import esthesis.avro.CommandType;
import esthesis.avro.ExecutionType;
import esthesis.common.AppConstants.Campaign.Condition.Op;
import esthesis.common.AppConstants.Campaign.Condition.Stage;
import esthesis.common.AppConstants.Campaign.Condition.Type;
import esthesis.common.AppConstants.Campaign.State;
import esthesis.common.entity.CommandReplyEntity;
import esthesis.common.exception.QMismatchException;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.GroupDTO;
import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.resource.CommandSystemResource;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import esthesis.services.campaign.impl.service.CampaignService;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ChoiceFormat;
import java.time.Instant;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class CampaignWorker {

  @Inject
  CampaignService campaignService;

  @Inject
  CampaignDeviceMonitorService campaignDeviceMonitorService;

  @Inject
  @RestClient
  CommandSystemResource commandSystemResource;

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
      log.debug("No pause condition found for campaign id '{}', group '{}'.", campaignId,
          groupExpression);
    }

    return pauseCondition;
  }

  public boolean dateTimeCondition(String campaignId, String groupExpression) {
    log.debug("dateTimeCondition, campaignId: {}, group: {}", campaignId, groupExpression);

    // Get the campaign details, conditions, and devices for this campaign.
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    GroupDTO groupDTO = new GroupDTO(groupExpression);
    List<CampaignConditionDTO> conditions = getCondition(campaignEntity, groupDTO, Type.DATETIME);
    if (CollectionUtils.isEmpty(conditions)) {
      log.debug("No date/time condition found for campaign id '{}', group '{}'.", campaignId,
          groupExpression);
      return true;
    } else {
      log.debug("Found '{}' date/time {}.", conditions.size(),
          new ChoiceFormat("0#conditions|1#condition|1<conditions").format(conditions.size()));
    }

    boolean dateTimeCondition = true;
    for (CampaignConditionDTO condition : conditions) {
      log.debug("Checking date/time condition '{}'.", condition);
      if (condition.getOperation() == Op.BEFORE) {
        if (!Instant.now().isBefore(condition.getScheduleDate())) {
          dateTimeCondition = false;
        }
      } else if (condition.getOperation() == Op.AFTER) {
        if (!Instant.now().isAfter(condition.getScheduleDate())) {
          dateTimeCondition = false;
        }
      } else {
        log.warn("Unsupported date/time condition operation '{}', will be skipped.",
            condition.getOperation());
      }

      if (!dateTimeCondition) {
        log.debug("Date/time condition evaluation failed, not all devices satisfy condition "
                + "'{}'.",
            condition);
        break;
      }
    }

    return dateTimeCondition;
  }

  @SuppressWarnings({"java:S2200", "java:S1121"})
  public boolean propertyCondition(String campaignId, String groupExpression) {
    log.debug("propertyCondition, campaignId: {}, group: {}", campaignId, groupExpression);

    // Get the campaign details, conditions, and devices for this campaign.
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    GroupDTO groupDTO = new GroupDTO(groupExpression);
    List<CampaignConditionDTO> conditions = getCondition(campaignEntity, groupDTO, Type.PROPERTY);
    if (CollectionUtils.isEmpty(conditions)) {
      log.debug("No property condition found for campaign id '{}', group '{}'.", campaignId,
          groupExpression);
      return true;
    } else {
      log.debug("Found '{}' property {}.", conditions.size(),
          new ChoiceFormat("0#conditions|1#condition|1<conditions").format(conditions.size()));
    }
    List<CampaignDeviceMonitorEntity> devices = campaignDeviceMonitorService.findByCampaignID(
        campaignId);
    log.debug("Found '{}' {}.", devices.size(),
        new ChoiceFormat("0#devices|1#device|1<devices").format(devices.size()));

    boolean propertyCondition = true;
    for (CampaignConditionDTO condition : conditions) {
      for (CampaignDeviceMonitorEntity device : devices) {
        log.debug("Checking property condition '{}' for device '{}'.", condition, device);
        // Get the property value of this condition from the device.
        String deviceValue = redisUtils.getFromHash(KeyType.ESTHESIS_DM, device.getHardwareId(),
            condition.getPropertyName());
        // If the property value is not found but the condition is ignorable, skip this check.
        // Otherwise, evaluate the condition.
        if (StringUtils.isBlank(deviceValue) && Boolean.TRUE.equals(
            condition.getPropertyIgnorable())) {
          log.debug("Property value not found but condition is ignorable, skipping this check.");
          continue;
        } else {
          if (StringUtils.isBlank(deviceValue)) {
            log.debug("Property value not found, setting condition to false.");
            propertyCondition = false;
          } else {
            // If the value given for this property is numeric, perform a numeric comparison.
            // Otherwise, perform a String comparison.
            if (StringUtils.isNumeric(condition.getValue())) {
              log.debug("Performing numeric comparison.");
              switch (condition.getOperation()) {
                case LT -> propertyCondition =
                    Double.parseDouble(deviceValue) < Double.parseDouble(condition.getValue());
                case LTE -> propertyCondition =
                    Double.parseDouble(deviceValue) <= Double.parseDouble(condition.getValue());
                case EQUAL -> propertyCondition =
                    Double.parseDouble(deviceValue) == Double.parseDouble(condition.getValue());
                case GT -> propertyCondition =
                    Double.parseDouble(deviceValue) > Double.parseDouble(condition.getValue());
                case GTE -> propertyCondition =
                    Double.parseDouble(deviceValue) >= Double.parseDouble(condition.getValue());
              }
            } else {
              System.out.println("Performing string comparison.");
              int comparisonResult = deviceValue.compareTo(condition.getValue());
              switch (condition.getOperation()) {
                case LT, LTE -> propertyCondition = comparisonResult < 0;
                case GT, GTE -> propertyCondition = comparisonResult > 0;
                case EQUAL -> propertyCondition = comparisonResult == 0;
              }
            }
            log.debug("Property condition result: {}", propertyCondition);
          }
        }
        if (!propertyCondition) {
          log.debug("Property condition evaluation failed, not all devices satisfy condition "
                  + "'{}'.",
              condition);
          break;
        }
      }
      if (!propertyCondition) {
        log.debug("Property condition evaluation failed, not all conditions satisfied.");
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

  /**
   * Find the batch size (i.e. how many devices are contacted in parallel) for a group.
   * <p><p>
   * - First, check if the group has a batch size defined.
   * <p>
   * - If not, check if the global group has a batch size defined.
   * <p>
   * - If not, all devices are contacted.
   *
   * @param campaignEntity The campaign entity to check for.
   * @param groupDTO       The group to check for.
   */
  private int findBatchSize(CampaignEntity campaignEntity, GroupDTO groupDTO) {
    Integer batchSize = null;

    // Find group-specific batch size.
    List<CampaignConditionDTO> conditions = getCondition(campaignEntity, groupDTO, Type.BATCH);
    if (conditions.size() == 1) {
      batchSize = Integer.parseInt(conditions.get(0).getValue());
    } else if (conditions.size() > 1) {
      log.warn("Found '{}' batch conditions for group '{}', using the first one.",
          conditions.size(), groupDTO);
      batchSize = Integer.parseInt(conditions.get(0).getValue());
    }
    if (batchSize != null) {
      log.debug("Found group-specific batch size '{}' for group '{}'.", batchSize, groupDTO);
      return batchSize;
    }

    // Find global-group batch size.
    GroupDTO globalGroup = new GroupDTO("group:0:inside");
    conditions = getCondition(campaignEntity, globalGroup, Type.BATCH);
    if (conditions.size() == 1) {
      batchSize = Integer.parseInt(conditions.get(0).getValue());
    } else if (conditions.size() > 1) {
      log.warn("Found '{}' batch conditions for group '{}', using the first one.",
          conditions.size(), groupDTO);
      batchSize = Integer.parseInt(conditions.get(0).getValue());
    }
    if (batchSize != null) {
      log.debug("Found global batch size '{}'.", batchSize);
      return batchSize;
    }

    // If not a specific batch size was specified, return the maximum possible value.
    return Integer.MAX_VALUE;
  }

  public void updateReplies(String campaignId, String groupExpression) {
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

  public void contactDevices(String campaignId, String groupExpression) {
    log.debug("contactDevices, campaignId: '{}', groupExpression: '{}'", campaignId,
        groupExpression);
    // Get the campaign details and parse the group expression.
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    GroupDTO groupDTO = new GroupDTO(groupExpression);

    // Find the batch size for this group.
    int batchSize = findBatchSize(campaignEntity, groupDTO);
    log.debug("Batch size is '{}'", batchSize);

    // Find the devices to contact.
    List<CampaignDeviceMonitorEntity> devices = campaignDeviceMonitorService.findNotContacted(
        campaignId, groupDTO.getGroup(), batchSize);
    log.debug("Found '{}' devices to contact.", devices.size());

    // Send the command to each device.
    // Since we want to individually track each device, we do not batch devices in a single command.
    for (CampaignDeviceMonitorEntity device : devices) {
      log.debug("Contacting device '{}'.", device.getHardwareId());
      CommandRequestEntity cmd = new CommandRequestEntity();
      cmd.setHardwareIds(device.getHardwareId());
      cmd.setCreatedOn(Instant.now());
      cmd.setExecutionType(ExecutionType.s);
      cmd.setDescription("Campaign " + campaignEntity.getName());
      switch (campaignEntity.getType()) {
        case PROVISIONING -> {
          cmd.setCommandType(CommandType.f);
          cmd.setCommand(campaignEntity.getProvisioningPackageId());
        }
        case EXECUTE_COMMAND -> {
          cmd.setCommandType(CommandType.e);
          cmd.setCommand(campaignEntity.getCommandName());
          cmd.setArguments(campaignEntity.getCommandArguments());
        }
        case REBOOT -> cmd.setCommandType(CommandType.r);
        case SHUTDOWN -> cmd.setCommandType(CommandType.s);
        case PING -> cmd.setCommandType(CommandType.p);
        default ->
            throw new QMismatchException("Unknown campaign type '{}'.", campaignEntity.getType());
      }
      ObjectId commandRequestId = new ObjectId(commandSystemResource.save(cmd));
      device.setCommandRequestId(commandRequestId);
      campaignDeviceMonitorService.save(device);

      log.debug("Created command request '{}' for device '{}'.", commandRequestId,
          device.getHardwareId());
    }
  }

  public boolean checkRate(String campaignId, String groupExpression) {
    log.debug("rateCondition, campaignId: {}, group: {}", campaignId, groupExpression);

    // Get the campaign details and parse the group expression.
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    GroupDTO groupDTO = new GroupDTO(groupExpression);

    // Get the requested rate number.
    List<CampaignConditionDTO> conditions = getCondition(campaignEntity, groupDTO, Type.SUCCESS);
    BigDecimal requestedRate;
    if (CollectionUtils.isEmpty(conditions)) {
      log.debug("No rate condition found for campaign id '{}', group '{}'.", campaignId,
          groupExpression);
      return true;
    } else if (conditions.size() > 1) {
      log.warn("Found '{}' rate conditions for campaign id '{}', group '{}', using the first one.",
          conditions.size(), campaignId, groupExpression);
      requestedRate = new BigDecimal(conditions.get(0).getValue());
    } else {
      log.debug("Found batch condition '{}' for campaign id '{}', group '{}'.",
          conditions.get(0), campaignId, groupExpression);
      requestedRate = new BigDecimal(conditions.get(0).getValue());
    }
    requestedRate = requestedRate.divide(new BigDecimal(100), 2, RoundingMode.FLOOR);

    // Compare the requested rate with the actual rate.
    BigDecimal actualRate = campaignDeviceMonitorService.checkRate(campaignId,
        groupDTO.getGroup());
    log.debug("Requested rate: '{}', actual rate: '{}'.", requestedRate, actualRate);

    return actualRate.compareTo(requestedRate) >= 0;
  }

  public boolean checkRemainingDevices(String campaignId, String groupExpression) {
    log.debug("checkRemainingDevice, campaignId: {}, group: {}", campaignId,
        groupExpression);
    GroupDTO groupDTO = new GroupDTO(groupExpression);
    return campaignDeviceMonitorService.hasUncontactedDevices(campaignId, groupDTO.getGroup());
  }
}
