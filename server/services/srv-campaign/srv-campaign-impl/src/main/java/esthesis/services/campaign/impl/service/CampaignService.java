package esthesis.services.campaign.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import esthesis.common.AppConstants.Campaign.Member.Type;
import esthesis.common.AppConstants.Campaign.State;
import esthesis.common.exception.QExceptionWrapper;
import esthesis.service.campaign.dto.CampaignMemberDTO;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.exception.CampaignDeviceAmbiguous;
import esthesis.service.campaign.exception.CampaignDeviceDoesNotExist;
import esthesis.service.common.BaseService;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.services.campaign.impl.worker.CampaignDeviceWorkflow;
import esthesis.util.kogito.client.KogitoClient;
import esthesis.util.kogito.dto.InstanceDTO;
import esthesis.util.kogito.dto.TaskDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class CampaignService extends BaseService<CampaignEntity> {

  @Inject
  JsonWebToken jwt;

  @Inject
  CampaignDeviceMonitorService campaignDeviceMonitorService;

  @Inject
  KogitoClient kogitoClient;

  @Inject
  @RestClient
  DeviceResource deviceResource;

  public static final String CAMPAIGN_PROCESS_ID = "DeviceCampaign";
  public static final String CAMPAIGN_ENTRY_EXIT_PROCESS_ID = "DeviceCampaignEntryExit";
  public static final String CAMPAIGN_ENTRY_EXIT_PAUSE_TASK = "ConditionalPause";

  @Override
  public CampaignEntity save(CampaignEntity campaignEntity) {
    if (campaignEntity.getState() == null) {
      campaignEntity.setState(State.CREATED);
    }
    return super.save(campaignEntity);
  }

  public void resume(String campaignId) {
    // Get campaign to resume.
    log.debug("Resuming campaign with id '{}'.", campaignId);
    CampaignEntity campaignEntity = findById(campaignId);

    // Find instances of the entry/exit workflow for this campaign.
    try {
      List<InstanceDTO> resources = kogitoClient.getInstances(CAMPAIGN_ENTRY_EXIT_PROCESS_ID);
      List<InstanceDTO> entryExitInstances = resources.stream()
          .filter(resource -> resource.getData().get("campaignId").equals(campaignId.toString()))
          .toList();
      if (CollectionUtils.isEmpty(entryExitInstances)) {
        log.warn("No entry/exit process instances found for campaign id '{}'", campaignId);
      } else if (entryExitInstances.size() > 1) {
        log.warn("More than one entry/exit process instances found for campaign id '{}'",
            campaignId);
      } else {
        entryExitInstances
            .forEach(resource -> {
              // Get the instance id.
              String entryExitInstanceId = resource.getId();

              // Get the tasks for this entry/exit process instance.
              List<TaskDTO> tasks = kogitoClient.getTasks(CAMPAIGN_ENTRY_EXIT_PROCESS_ID,
                  entryExitInstanceId);
              if (tasks.size() > 1) {
                log.warn("More than one tasks found for campaign id '{}', entry/exit process "
                        + "instance id '{}'",
                    campaignId, entryExitInstanceId);
              }

              tasks.forEach(task -> {
                // Get the task id.
                String taskId = task.getId();

                // Complete the task.
                try {
                  kogitoClient.completeTask(CAMPAIGN_ENTRY_EXIT_PROCESS_ID, entryExitInstanceId,
                      CAMPAIGN_ENTRY_EXIT_PAUSE_TASK, taskId);
                } catch (JsonProcessingException e) {
                  throw new QExceptionWrapper("Could not resume campaign with id '" +
                      campaignId + "'.", e);
                }
              });
            });
      }
    } catch (JsonProcessingException e) {
      throw new QExceptionWrapper("Could not resume campaign.", e);
    }
  }

  public List<Integer> findGroups(String campaignId) {
    CampaignEntity campaign = findById(campaignId);
    int groupsNo =
        campaign.getMembers().stream()
            .max(Comparator.comparing(CampaignMemberDTO::getGroup))
            .orElseGet(CampaignMemberDTO::new).getGroup();
    List<Integer> groups = new ArrayList<>(groupsNo);
    for (int i = 1; i <= groupsNo; i++) {
      groups.add(i);
    }

    return groups;
  }

  public CampaignStatsDTO getCampaignStats(String campaignId) {
    CampaignStatsDTO campaignStatsDTO = new CampaignStatsDTO();
    CampaignEntity campaignEntity = findById(campaignId);

    campaignStatsDTO.setStateDescription(campaignEntity.getStateDescription());

    // Find the group members of each campaign group.
    int campaignGroups = findGroups(campaignId).size();
    List<Integer> groupMembers = new ArrayList<>();
    for (int i = 1; i <= campaignGroups; i++) {
      groupMembers.add(campaignDeviceMonitorService.findByColumn("group", i).size());
    }
    campaignStatsDTO.setGroupMembers(groupMembers);

    // Find group members replies (how many group members have replied per group).
    List<Long> groupMembersReplied = new ArrayList<>();
    for (int i = 1; i <= campaignGroups; i++) {
      groupMembersReplied.add(campaignDeviceMonitorService.countReplies(campaignId, i));
    }
    campaignStatsDTO.setGroupMembersReplied(groupMembersReplied);

    // Find all members, contacted and replied.
    campaignStatsDTO.setMembersContactedButNotReplied(
        campaignDeviceMonitorService.countContactedNotReplied(campaignId));
    campaignStatsDTO.setMembersReplied(campaignDeviceMonitorService.countReplies(campaignId));
    campaignStatsDTO.setAllMembers(campaignDeviceMonitorService.countAll(campaignId));
    campaignStatsDTO.setMembersContacted(campaignDeviceMonitorService.countContacted(campaignId));

    // Calculate success rate.
    campaignStatsDTO.setSuccessRate(
        new BigDecimal(campaignStatsDTO.getMembersReplied())
            .divide(new BigDecimal(campaignStatsDTO.getAllMembers()), 1, RoundingMode.DOWN)
            .multiply(new BigDecimal(100)));

    // Calculate progress.
    campaignStatsDTO.setProgress(
        new BigDecimal(campaignStatsDTO.getMembersContacted())
            .divide(new BigDecimal(campaignStatsDTO.getAllMembers()), 1, RoundingMode.DOWN)
            .multiply(new BigDecimal(100)));

    // Calculate duration and ETA.
    long diff;
    if (campaignEntity.getState() != State.TERMINATED_BY_USER
        && campaignEntity.getState() != State.TERMINATED_BY_WORKFLOW) {
      diff = Instant.now().toEpochMilli() - campaignEntity.getStartedOn().toEpochMilli();
    } else {
      diff =
          campaignEntity.getTerminatedOn().toEpochMilli() - campaignEntity.getStartedOn()
              .toEpochMilli();
    }
    campaignStatsDTO.setDuration(DurationFormatUtils.formatDurationWords(diff, true, true));

    BigDecimal eta = new BigDecimal(campaignStatsDTO.getMembersReplied())
        .divide(new BigDecimal(campaignStatsDTO.getAllMembers()), 1, RoundingMode.DOWN)
        .multiply(new BigDecimal(100));

    if (eta.compareTo(new BigDecimal(0)) > 0) {
      campaignStatsDTO.setEta(DurationFormatUtils.formatDurationWords(eta.longValue(), true, true));
    } else {
      campaignStatsDTO.setEta("-");
    }

    return campaignStatsDTO;
  }

  public void start(String campaignId) {
    // Get the campaign about to start.
    log.debug("Starting campaign with id '{}'.", campaignId);
    CampaignEntity campaignEntity = findById(campaignId);

    // Create monitoring entries for each device of this campaign.
    for (CampaignMemberDTO member : campaignEntity.getMembers()) {
      if (member.getType() == Type.DEVICE) {
        log.debug("Searching device with hardware id '{}'.", member.getIdentifier());
        // Find the device behind this member's identifier.
        List<DeviceEntity> device = deviceResource.findByHardwareIds(member.getIdentifier(),
            false);

        // Check if a unique device was found.
        if (device.size() == 0) {
          log.debug("Device with hardware id '{}' was not found.", member.getIdentifier());
          throw new CampaignDeviceDoesNotExist("Device with hardware id '{}' can not be found.",
              member.getIdentifier());
        } else if (device.size() > 1) {
          log.debug("Found multiple devices matching hardware id '{}'.",
              member.getIdentifier());
          throw new CampaignDeviceAmbiguous("Device with hardware id '{}' matches multiple "
              + "devices.", member.getIdentifier());
        }

        // Create a monitoring entry for this device.
        log.debug("Adding device with hardware id '{}' to campaign '{}'.",
            member.getIdentifier(), campaignId);
        CampaignDeviceMonitorEntity cdm = new CampaignDeviceMonitorEntity();
        cdm.setDeviceId(device.get(0).getId());
        cdm.setHardwareId(device.get(0).getHardwareId());
        cdm.setCampaignId(new ObjectId(campaignId));
        cdm.setGroup(member.getGroup());
        campaignDeviceMonitorService.save(cdm);
      } else {
        log.debug("Adding devices matching tag '{}' to campaign '{}'.",
            member.getIdentifier(), campaignId);
        deviceResource.findByTagName(member.getIdentifier()).forEach(device -> {
          log.debug("Adding device with hardware id '{}' to campaign '{}'.",
              device.getHardwareId(), campaignId);
          CampaignDeviceMonitorEntity cdm = new CampaignDeviceMonitorEntity();
          cdm.setDeviceId(device.getId());
          cdm.setHardwareId(device.getHardwareId());
          cdm.setCampaignId(new ObjectId(campaignId));
          cdm.setGroup(member.getGroup());
          campaignDeviceMonitorService.save(cdm);
        });
      }
    }

    // Start the workflow for this campaign.
    log.debug("Starting campaign workflow for campaign id '{}'.", campaignId);
    try {
      CampaignDeviceWorkflow campaignDeviceWorkflow = new CampaignDeviceWorkflow();
      campaignDeviceWorkflow.setCampaignId(campaignId);
      InstanceDTO instanceDTO = kogitoClient.startInstance(CAMPAIGN_PROCESS_ID,
          campaignDeviceWorkflow);
      campaignEntity.setState(State.RUNNING);
      campaignEntity.setStartedOn(Instant.now());
      campaignEntity.setProcessInstanceId(instanceDTO.getId());
      campaignEntity.setStateDescription("Campaign started at " + Instant.now() + ".");
      super.save(campaignEntity);

      log.info("Started workflow instance '{}' for campaign id '{}'.",
          instanceDTO.getId(), campaignId);
    } catch (JsonProcessingException e) {
      throw new QExceptionWrapper("Could not parse Kogito reply.", e);
    }
  }

  public void delete(String campaignId) {
    deleteById(campaignId);
    campaignDeviceMonitorService.deleteByColumn("campaignId", campaignId);
  }

}
