package esthesis.platform.server.service;

import esthesis.platform.server.config.AppConstants;
import esthesis.platform.server.config.AppConstants.Campaign.Condition.Stage;
import esthesis.platform.server.config.AppConstants.Campaign.State;
import esthesis.platform.server.dto.CampaignConditionDTO;
import esthesis.platform.server.dto.CampaignDTO;
import esthesis.platform.server.dto.CampaignMemberDTO;
import esthesis.platform.server.dto.CampaignStatsDTO;
import esthesis.platform.server.dto.device.DeviceDTO;
import esthesis.platform.server.mapper.CampaignConditionMapper;
import esthesis.platform.server.mapper.CampaignMapper;
import esthesis.platform.server.mapper.CampaignMemberMapper;
import esthesis.platform.server.model.Campaign;
import esthesis.platform.server.model.CampaignCondition;
import esthesis.platform.server.model.CampaignDeviceMonitor;
import esthesis.platform.server.model.CampaignMember;
import esthesis.platform.server.model.CommandReply;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.CampaignConditionRepository;
import esthesis.platform.server.repository.CampaignDeviceMonitorRepository;
import esthesis.platform.server.repository.CampaignMemberRepository;
import esthesis.platform.server.repository.CampaignRepository;
import esthesis.platform.server.repository.CommandReplyRepository;
import esthesis.platform.server.workflow.CWFLConstants;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Campaign management functionality.
 */
@Log
@Service
@Validated
@Transactional
public class CampaignService extends BaseService<CampaignDTO, Campaign> {

  private final CampaignMapper campaignMapper;
  private final CampaignMemberMapper campaignMemberMapper;
  private final CampaignConditionMapper campaignConditionMapper;
  private final CampaignConditionRepository campaignConditionRepository;
  private final CampaignMemberRepository campaignMemberRepository;
  private final DeviceService deviceService;
  private final CampaignRepository campaignRepository;
  private final CampaignDeviceMonitorRepository campaignDeviceMonitorRepository;
  private final CommandReplyRepository commandReplyRepository;

  public CampaignService(CampaignMapper campaignMapper,
    CampaignMemberMapper campaignMemberMapper,
    CampaignConditionMapper campaignConditionMapper,
    CampaignConditionRepository campaignConditionRepository,
    CampaignMemberRepository campaignMemberRepository, DeviceService deviceService,
    CampaignRepository campaignRepository,
    CampaignDeviceMonitorRepository campaignDeviceMonitorRepository,
    CommandReplyRepository commandReplyRepository) {
    this.campaignMapper = campaignMapper;
    this.campaignMemberMapper = campaignMemberMapper;
    this.campaignConditionMapper = campaignConditionMapper;
    this.campaignConditionRepository = campaignConditionRepository;
    this.campaignMemberRepository = campaignMemberRepository;
    this.deviceService = deviceService;
    this.campaignRepository = campaignRepository;
    this.campaignDeviceMonitorRepository = campaignDeviceMonitorRepository;
    this.commandReplyRepository = commandReplyRepository;
  }

  /**
   * Saves a campaign.
   * @param campaignDTO The campaign information to save.
   */
  public CampaignDTO save(CampaignDTO campaignDTO) {
    final Campaign campaign;
    // Modify an existing or create a new campaign.
    if (campaignDTO.getId() != null) {
      campaign = findEntityById(campaignDTO.getId());
      campaignMapper.map(campaignDTO, campaign);
      campaign.getConditions().clear();
      campaign.getMembers().clear();
    } else {
      campaign = campaignMapper.map(campaignDTO);
      campaign.setState(State.CREATED);
    }

    // Map campaign conditions.
    for (CampaignConditionDTO campaignConditionDTO : campaignDTO.getConditions()) {
      CampaignCondition campaignCondition = campaignConditionMapper.map(campaignConditionDTO);
      campaignCondition.setCampaign(campaign);
      campaign.getConditions().add(campaignCondition);
    }

    // Map campaign members.
    for (CampaignMemberDTO campaignMemberDTO : campaignDTO.getMembers()) {
      CampaignMember campaignMember = campaignMemberMapper.map(campaignMemberDTO);
      campaignMember.setCampaign(campaign);
      campaign.getMembers().add(campaignMember);
    }

    // Save campaign.
    return campaignMapper.map(getRepository().save(campaign));
  }

  /**
   * Creates a new instance for a campaign. This method only affects the state of the campaign in
   * esthesis database; the workflow engine needs to be called in a separate call in order to
   * instantiate the actual workflow instance (this is necessary in order to start the workflow
   * instance on a separate, non-blocking thread).
   * @param campaignId The Id of the campaign to start.
   */
  public void instantiate(long campaignId) {
    Campaign campaign = findEntityById(campaignId);

    // Find the devices belonging to the campaign and create monitoring entries for them in the
    // database.
    int campaignGroups = countCampaignGroups(campaignId);
    for (int i = 1; i <= campaignGroups; i++) {
      List<String> hardwareIds = findHardwareIdsForGroup(campaignId, i);
      for (String hardwareId : hardwareIds) {
        Optional<Device> device = deviceService.findEntityByHardwareId(hardwareId);
        if (device.isPresent()) {
          CampaignDeviceMonitor cdm = new CampaignDeviceMonitor();
          cdm.setCampaign(campaign);
          cdm.setDevice(device.get());
          cdm.setGroupOrder(i);
          campaignDeviceMonitorRepository.save(cdm);
        }
      }
    }

    // Set campaign as running.
    campaign.setState(State.RUNNING);
    campaign.setStartedOn(Instant.now());
  }

  /**
   * Sets the state of the campaign. Note that setting the state of the campaign with this method
   * only affects
   * @param campaignId
   * @param state
   */
  public void setState(long campaignId, int state) {
    findEntityById(campaignId)
      .setState(state)
      .setTerminatedOn(Instant.now());
  }

  /**
   * Finds the workflow condition associated with a specific workflow location.
   *
   * @param campaignId    The Id of the underlying campaign.
   * @param tokenLocation The current location of the token (see {@link
   * @param conditionType The type of the condition (AppConstants.Condition.Type)
   *                      esthesis.platform.server.workflow.ConditionsHelper}.
   */
  public List<CampaignConditionDTO> findConditions(Long campaignId, String tokenLocation,
    int conditionType) {
    Integer target = null;
    Integer stage = null;

    if (tokenLocation.equals(CWFLConstants.ACTIVITY_GLOBAL_ENTRY_ID)) {
      target = 0;
      stage = Stage.ENTRY;
    } else if (tokenLocation.startsWith(CWFLConstants.ACTIVITY_GROUP_ENTRY_PREFIX)) {
      target = Integer.parseInt(tokenLocation.substring(tokenLocation.lastIndexOf("_") + 1)) + 1;
      stage = Stage.ENTRY;
    } else if (tokenLocation.startsWith(CWFLConstants.ACTIVITY_GROUP_PROCESS_PREFIX)) {
      target = Integer.parseInt(tokenLocation.substring(tokenLocation.lastIndexOf("_") + 1)) + 1;
    } else if (tokenLocation.startsWith(CWFLConstants.ACTIVITY_GROUP_EXIT_PREFIX)) {
      target = Integer.parseInt(tokenLocation.substring(tokenLocation.lastIndexOf("_") + 1)) + 1;
      stage = Stage.EXIT;
    } else if (tokenLocation.equals(CWFLConstants.ACTIVITY_GLOBAL_EXIT_ID)) {
      target = 0;
      stage = Stage.EXIT;
    }

    if (stage == null) {
      return campaignConditionMapper
        .map(campaignConditionRepository
          .findByCampaignIdAndTargetAndType(campaignId, target, conditionType));
    } else {
      return campaignConditionMapper
        .map(campaignConditionRepository
          .findByCampaignIdAndTargetAndStageAndType(campaignId, target, stage, conditionType));
    }
  }

  /**
   * Finds the campaign members assigned to a specific group of a campaign. This method does not
   * resolve tags into members, it simply enlists all members as assigned in the campaign edit
   * screen.
   *
   * @param campaignId The Id of the campaign to find the members of.
   * @param group The group order to find the members of.
   */
  public List<CampaignMemberDTO> findCampaignMembersForGroup(long campaignId, int group) {
    return campaignMemberMapper.map(
      campaignMemberRepository.findByCampaignIdAndGroupOrder(campaignId, group));
  }

  /**
   * Finds the campaign members assigned to a specific campaign. This method does not resolve tags
   * into members, it simply enlists all members as assigned in the campaign edit screen.
   *
   * @param campaignId The Id of the campaign to find the members of.
   */
  public List<CampaignMemberDTO> findCampaignMembersForCampaign(long campaignId) {
    return campaignMemberMapper.map(campaignMemberRepository.findByCampaignId(campaignId));
  }

  /**
   * Finds the hardware Ids of the devices belonging to a campaign group. If the group contains
   * tags, the tags are resolved and the devices associated with those tags are included in the
   * returned list.
   *
   * @param campaignId The Id of the campaign to find the hardware Ids of.
   * @param group The group order to find the memers of.
   */
  public List<String> findHardwareIdsForGroup(long campaignId, int group) {
    final List<String> hardwareIds = new ArrayList<>();
    findCampaignMembersForGroup(campaignId, group).forEach(
      campaignMember -> {
        if (campaignMember.getType() == AppConstants.Campaign.Member.Type.DEVICE) {
          hardwareIds.add(campaignMember.getIdentifier());
        } else {
          hardwareIds.addAll(deviceService.findByTags(campaignMember.getIdentifier().split(","))
            .stream().map(DeviceDTO::getHardwareId).collect(Collectors.toList()));
        }
      }
    );

    return hardwareIds.stream().distinct().collect(Collectors.toList());
  }

  /**
   * Finds the hardware Ids of the devices belonging to a campaign. If the campaign contains tags,
   * the tags are resolved and the devices associated with those tags are included in the returned
   * list.
   *
   * @param campaignId The Id of the campaign to find the hardware Ids of.
   */
  public List<String> findHardwareIdsForCampaign(long campaignId) {
    final List<String> hardwareIds = new ArrayList<>();
    findEntityById(campaignId).getMembers().forEach(campaignMember -> {
      if (campaignMember.getType() == AppConstants.Campaign.Member.Type.DEVICE) {
        hardwareIds.add(campaignMember.getIdentifier());
      } else {
        hardwareIds.addAll(deviceService.findByTags(campaignMember.getIdentifier().split(","))
          .stream().map(DeviceDTO::getHardwareId).collect(Collectors.toList()));
      }
    });

    return hardwareIds.stream().distinct().collect(Collectors.toList());
  }

  /**
   * Finds the number of groups associated with the campaign. Global group is not counted in, so if
   * a campaign has just a single group, the result will be 1.
   *
   * @param campaignId The campaign to find the number of groups for.
   * @return Returns the number of groups on the campaign without counting the global group.
   */
  public int countCampaignGroups(long campaignId) {
    return campaignRepository.countCampaignGroups(campaignId);
  }

  /**
   * Finds the batch size (i.e. how many devices should be updated simultaneously) for the specific
   * target of a campaign. If the requested target does not have a batch size specified, the global
   * batch size is seeked instead. If a global batch size does not exist either, an
   * Integer.MAX_VALUE is returned instead.
   *
   * @param campaignId The Id of the campaign to find the batch size for.
   * @param target     The target of the campaign to find the batch size for.
   * @return The number of devices that should be updated simultaneously.
   */
  public int findBatchSize(long campaignId, int target) {
    int batchSize = Integer.MAX_VALUE;
    List<CampaignCondition> batchConditions =
      campaignConditionRepository.findByCampaignIdAndTargetAndType(campaignId, target,
        AppConstants.Campaign.Condition.Type.BATCH);
    if (batchConditions.size() == 0) {
      batchConditions = campaignConditionRepository.findByCampaignIdAndTargetAndType(campaignId, 0,
        AppConstants.Campaign.Condition.Type.BATCH);
      if (batchConditions.size() > 0) {
        batchSize = Integer.parseInt(batchConditions.get(0).getValue());
      }
    } else {
      batchSize = Integer.parseInt(batchConditions.get(0).getValue());
    }

    return batchSize;
  }

  /**
   * Gather various statistics on a campaign.
   *
   * @param campaignId The campaign Id to gether statistics for.
   */
  public CampaignStatsDTO statsCampaign(long campaignId) {
    Campaign campaign = findEntityById(campaignId);
    CampaignStatsDTO campaignStatsDTO = new CampaignStatsDTO();

    // Find the group members of each campaign group.
    int campaignGroups = campaignRepository.countCampaignGroups(campaignId);
    List<Integer> groupMembers = new ArrayList<>();
    for (int i = 1; i <= campaignGroups; i++) {
      groupMembers.add(findHardwareIdsForGroup(campaignId, i).size());
    }
    campaignStatsDTO.setGroupMembers(groupMembers);

    // Find group members replies (how many group members have replied per group).
    List<Integer> groupMembersReplied = new ArrayList<>();
    for (int i = 1; i <= campaignGroups; i++) {
      groupMembersReplied.add(campaignDeviceMonitorRepository
        .countAllByCampaignIdAndGroupOrderAndCommandRequestIdNotNullAndCommandReplyNotNull(
          campaignId, i));
    }
    campaignStatsDTO.setGroupMembersReplied(groupMembersReplied);

    // Find all members, contacted and replied.
    campaignStatsDTO.setMembersContactedButNotReplied(campaignDeviceMonitorRepository
      .countAllByCampaignIdAndCommandRequestIdNotNullAndCommandReplyNull(campaignId));
    campaignStatsDTO.setMembersReplied(campaignDeviceMonitorRepository
      .countAllByCampaignIdAndCommandRequestIdNotNullAndCommandReplyNotNull(campaignId));
    campaignStatsDTO
      .setAllMembers(campaignDeviceMonitorRepository.countAllByCampaignId(campaignId));
    campaignStatsDTO.setMembersContacted(
      campaignDeviceMonitorRepository.countAllByCampaignIdAndCommandRequestIdNotNull(campaignId));

    // Calculate success rate.
    campaignStatsDTO.setSuccessRate(
      (campaignStatsDTO.getMembersReplied() * 100) / campaignStatsDTO.getAllMembers());

    // Calculate progress.
    campaignStatsDTO.setProgress(
      (campaignStatsDTO.getMembersContacted() * 100) / campaignStatsDTO.getAllMembers());

    // Calculate duration and ETA.
    long diff;
    if (campaign.getState() != State.TERMINATED_BY_USER
      && campaign.getState() != State.TERMINATED_BY_WORKFLOW) {
      diff = Instant.now().toEpochMilli() - campaign.getStartedOn().toEpochMilli();
    } else {
      diff = campaign.getTerminatedOn().toEpochMilli() - campaign.getStartedOn().toEpochMilli();
    }
    campaignStatsDTO.setDuration(DurationFormatUtils.formatDurationWords(diff, true, true));
    long eta = (diff / campaignStatsDTO.getMembersReplied()) * (campaignStatsDTO.getAllMembers()
      - campaignStatsDTO.getMembersReplied());
    if (eta > 0 ) {
      campaignStatsDTO.setEta(DurationFormatUtils.formatDurationWords(eta, true, true));
    } else {
      campaignStatsDTO.setEta("-");
    }

    return campaignStatsDTO;
  }

  /**
   * Checks device replies received in the course of a campaign and updates the participants of a
   * campaign accordingly.
   *
   * @param campaignId The campaign to check replies for.
   */
  public void updateDeviceReplies(long campaignId) {
    List<CampaignDeviceMonitor> cdms = campaignDeviceMonitorRepository
      .findAllByCampaignIdAndCommandRequestIdNotNullAndCommandReplyNull(campaignId);
    for (CampaignDeviceMonitor cdm : cdms) {
      CommandReply reply = commandReplyRepository.findByCommandRequestId(cdm.getCommandRequestId());
      if (reply != null) {
        cdm.setCommandReply(reply);
      }
    }
  }

  /**
   * Get the current state of a campaign.
   * @param campaignId The Id of the campaign to find the state of.
   * @return See {@link AppConstants.Campaign.State}
   */
  public int getState(long campaignId) {
    return findEntityById(campaignId).getState();
  }

  /**
   * Delete a campaign. Note that deleting a campaign via this method only affects the esthesis
   * database; a separate call to the workflow engine is required to delete the workflow instance
   * associated with this campaign.
   * @param campaignId The Id of the campaign to delete.
   */
  public String delete(long campaignId) {
    Campaign campaign = findEntityById(campaignId);
    String processInstanceId = campaign.getProcessInstanceId();
    deleteById(campaignId);

    return processInstanceId;
  }
}

