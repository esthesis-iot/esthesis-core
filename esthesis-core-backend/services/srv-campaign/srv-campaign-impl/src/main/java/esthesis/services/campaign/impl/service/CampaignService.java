package esthesis.services.campaign.impl.service;

import esthesis.common.AppConstants.Campaign.Condition;
import esthesis.common.AppConstants.Campaign.Member.Type;
import esthesis.common.AppConstants.Campaign.State;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.CampaignMemberDTO;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.exception.CampaignDeviceAmbiguous;
import esthesis.service.campaign.exception.CampaignDeviceDoesNotExist;
import esthesis.service.common.BaseService;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.services.campaign.impl.dto.GroupDTO;
import esthesis.services.campaign.impl.job.WorkflowParameters;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@Transactional
@ApplicationScoped
public class CampaignService extends BaseService<CampaignEntity> {

	@Inject
	ZeebeClient zeebeClient;

	@Inject
	CampaignDeviceMonitorService campaignDeviceMonitorService;

	@Inject
	@RestClient
	DeviceResource deviceResource;

	public static final String CAMPAIGN_PROCESS_ID = "DeviceCampaignProcess";

	@Override
	public CampaignEntity save(CampaignEntity campaignEntity) {
		if (campaignEntity.getState() == null) {
			campaignEntity.setState(State.CREATED);
			campaignEntity.setCreatedOn(Instant.now());
		}
		return super.save(campaignEntity);
	}

	public void resume(String campaignId) {
		// Get campaign to resume.
		log.debug("Resuming campaign with id '{}'.", campaignId);

		zeebeClient.newPublishMessageCommand()
			.messageName(WorkflowParameters.MESSAGE_CONDITIONAL_PAUSE)
			.correlationKey(campaignId)
			.send()
			.join();
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

		// Set the state of this campaign.
		campaignStatsDTO.setStateDescription(campaignEntity.getStateDescription());

		// Find the group members of each campaign group.
		int campaignGroups = findGroups(campaignId).size();
		List<Long> groupMembers = new ArrayList<>();
		for (int i = 1; i <= campaignGroups; i++) {
			groupMembers.add(campaignDeviceMonitorService.countInGroup(campaignId, i));
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
		WorkflowParameters p = new WorkflowParameters();
		p.setCampaignId(campaignId);
		ProcessInstanceEvent processInstanceEvent = zeebeClient.newCreateInstanceCommand()
			.bpmnProcessId(CAMPAIGN_PROCESS_ID)
			.latestVersion()
			.variables(p)
			.send()
			.join();

		// Update the campaign in the database.
		campaignEntity.setState(State.RUNNING);
		campaignEntity.setStartedOn(Instant.now());
		campaignEntity.setProcessInstanceId(
			String.valueOf(processInstanceEvent.getProcessInstanceKey()));
		campaignEntity.setStateDescription("Campaign started at " + Instant.now() + ".");
		super.save(campaignEntity);
	}

	public void delete(String campaignId) {
		try {
			terminate(campaignId);
		} catch (ClientStatusException e) {
			log.warn("Could not delete campaign instance in workflow engine.", e);
		}
		deleteById(campaignId);
		campaignDeviceMonitorService.deleteByColumn("campaignId", campaignId);
	}

	public void terminate(String campaignId) {
		CampaignEntity campaignEntity = findById(campaignId);

		// Terminate the workflow for this campaign.
		if (StringUtils.isNotEmpty(campaignEntity.getProcessInstanceId())) {
			zeebeClient
				.newCancelInstanceCommand(Long.parseLong(campaignEntity.getProcessInstanceId()))
				.send()
				.join();
		}

		// Update the campaign state.
		campaignEntity.setState(State.TERMINATED_BY_USER);
		campaignEntity.setTerminatedOn(Instant.now());
		campaignEntity.setStateDescription("Campaign terminated by user at " + Instant.now() + ".");
		super.save(campaignEntity);
	}

	/**
	 * Replicate a campaign.
	 *
	 * @param campaignId The id of the campaign to replicate.
	 * @return The id of the new campaign.
	 */
	public CampaignEntity replicate(String campaignId) {
		CampaignEntity campaignEntity = findById(campaignId);

		// Create a new campaign.
		return save(new CampaignEntity()
			.setName(campaignEntity.getName() + " (replicated)")
			.setDescription(campaignEntity.getDescription())
			.setType(campaignEntity.getType())
			.setState(State.CREATED)
			.setConditions(campaignEntity.getConditions())
			.setMembers(campaignEntity.getMembers())
			.setCommandName(campaignEntity.getCommandName())
			.setCommandArguments(campaignEntity.getCommandArguments())
			.setCommandExecutionType(campaignEntity.getCommandExecutionType())
			.setProvisioningPackageId(campaignEntity.getProvisioningPackageId())
			.setAdvancedDateTimeRecheckTimer(campaignEntity.getAdvancedDateTimeRecheckTimer())
			.setAdvancedPropertyRecheckTimer(campaignEntity.getAdvancedPropertyRecheckTimer())
			.setAdvancedUpdateRepliesFinalTimer(campaignEntity.getAdvancedUpdateRepliesFinalTimer())
			.setAdvancedUpdateRepliesTimer(campaignEntity.getAdvancedUpdateRepliesTimer()));
	}

	public List<CampaignConditionDTO> getCondition(CampaignEntity campaignEntity,
		GroupDTO groupDTO, Condition.Type type) {
		return campaignEntity.getConditions().stream()
			.filter(condition -> condition.getType() == type)
			.filter(condition -> condition.getGroup() == groupDTO.getGroup())
			.filter(condition -> condition.getStage() == groupDTO.getStage())
			.toList();
	}

	public CampaignEntity setStateDescription(String campaignId, String stateDescription) {
		log.debug("Setting state description for campaign id '{}' to '{}'.", campaignId,
			stateDescription);
		CampaignEntity campaignEntity = findById(campaignId);
		campaignEntity.setStateDescription(stateDescription);
		return save(campaignEntity);
	}
}
