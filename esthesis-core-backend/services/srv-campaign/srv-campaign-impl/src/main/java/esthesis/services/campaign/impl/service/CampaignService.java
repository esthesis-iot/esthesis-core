package esthesis.services.campaign.impl.service;

import static esthesis.core.common.AppConstants.ROLE_SYSTEM;
import static esthesis.core.common.AppConstants.Security.Category.CAMPAIGN;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;
import static esthesis.services.campaign.impl.dto.ValidationMessages.DATE_IN_PAST;
import static esthesis.services.campaign.impl.dto.ValidationMessages.DATE_REQUIRED;
import static esthesis.services.campaign.impl.dto.ValidationMessages.GENERIC;
import static esthesis.services.campaign.impl.dto.ValidationMessages.OPERATION_REQUIRED;
import static esthesis.services.campaign.impl.dto.ValidationMessages.POSITIVE_INTEGER;
import static esthesis.services.campaign.impl.dto.ValidationMessages.PROPERTY_NAME_REQUIRED;
import static esthesis.services.campaign.impl.dto.ValidationMessages.STAGE_REQUIRED;

import esthesis.core.common.AppConstants.Campaign.Condition;
import esthesis.core.common.AppConstants.Campaign.Condition.Op;
import esthesis.core.common.AppConstants.Campaign.Member.Type;
import esthesis.core.common.AppConstants.Campaign.State;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.CampaignMemberDTO;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.exception.CampaignDeviceAmbiguous;
import esthesis.service.campaign.exception.CampaignDeviceDoesNotExist;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.common.validation.CVEBuilder;
import esthesis.service.common.validation.SoftValidators;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.security.annotation.ErnPermission;
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

	private String createValidationPath(CampaignConditionDTO campaignConditionDTO) {
		return String.valueOf(campaignConditionDTO.getType());
	}

	private CampaignEntity saveHandler(CampaignEntity campaignEntity) {
		CVEBuilder<String> violations = new CVEBuilder<>();
		List<CampaignConditionDTO> conditions = campaignEntity.getConditions();
		for (CampaignConditionDTO condition : conditions) {
			switch (condition.getType()) {
				case SUCCESS:
					if (condition.getValue() == null) {
						violations.add(createValidationPath(condition), GENERIC);
					}
					break;
				case PROPERTY:
					if (condition.getStage() == null) {
						violations.add(createValidationPath(condition), STAGE_REQUIRED);
					}
					if (StringUtils.isBlank(condition.getPropertyName())) {
						violations.add(createValidationPath(condition), PROPERTY_NAME_REQUIRED);
					}
					if (condition.getOperation() == null) {
						violations.add(createValidationPath(condition), OPERATION_REQUIRED);
					}
					if (condition.getValue() == null) {
						violations.add(createValidationPath(condition), GENERIC);
					}
					break;
				case PAUSE:
					if (condition.getStage() == null) {
						violations.add(createValidationPath(condition), STAGE_REQUIRED);
					}
					if (condition.getOperation() == null) {
						violations.add(createValidationPath(condition), OPERATION_REQUIRED);
					}
					break;
				case BATCH:
					if (SoftValidators.isNotPositiveInteger(condition.getValue())) {
						violations.add(createValidationPath(condition), POSITIVE_INTEGER);
					}
					break;
				case DATETIME:
					if (condition.getStage() == null) {
						violations.add(createValidationPath(condition), STAGE_REQUIRED);
					}
					if (condition.getOperation() == null) {
						violations.add(createValidationPath(condition), OPERATION_REQUIRED);
					}
					if (condition.getScheduleDate() == null) {
						violations.add(createValidationPath(condition), DATE_REQUIRED);
					}
					if (condition.getScheduleDate() != null
						&& condition.getScheduleDate().isBefore(Instant.now())
						&& condition.getOperation() == Op.BEFORE) {
						violations.add(createValidationPath(condition), DATE_IN_PAST);
					}
					break;
				default:
					violations.add(createValidationPath(condition), GENERIC);
					break;
			}
		}
		violations.throwCVE();

		if (campaignEntity.getState() == null) {
			campaignEntity.setState(State.CREATED);
			campaignEntity.setCreatedOn(Instant.now());
		}

		return super.save(campaignEntity);
	}

	@Transactional
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = CREATE)
	public CampaignEntity saveNew(CampaignEntity campaignEntity) {
		return saveHandler(campaignEntity);
	}

	@Transactional
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = WRITE)
	public CampaignEntity saveUpdate(CampaignEntity campaignEntity) {
		return saveHandler(campaignEntity);
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = WRITE)
	public void resume(String campaignId) {
		// Get campaign to resume.
		log.debug("Resuming campaign with id '{}'.", campaignId);

		zeebeClient.newPublishMessageCommand()
			.messageName(WorkflowParameters.MESSAGE_CONDITIONAL_PAUSE)
			.correlationKey(campaignId)
			.send()
			.join();
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = READ)
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

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = READ)
	public CampaignStatsDTO getCampaignStats(String campaignId) {
		CampaignStatsDTO campaignStatsDTO = new CampaignStatsDTO();
		CampaignEntity campaignEntity = findById(campaignId);

		// Set the state of this campaign.
		campaignStatsDTO.setStateDescription(campaignEntity.getStateDescription());
		campaignStatsDTO.setState(campaignEntity.getState());
		campaignStatsDTO.setCreatedOn(campaignEntity.getCreatedOn());
		campaignStatsDTO.setStartedOn(campaignEntity.getStartedOn());
		campaignStatsDTO.setTerminatedOn(campaignEntity.getTerminatedOn());

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
		if (campaignStatsDTO.getAllMembers() == 0) {
			campaignStatsDTO.setSuccessRate(BigDecimal.valueOf(0));
		} else {
			campaignStatsDTO.setSuccessRate(
				new BigDecimal(campaignStatsDTO.getMembersReplied())
					.divide(new BigDecimal(campaignStatsDTO.getAllMembers()), 1, RoundingMode.DOWN)
					.multiply(new BigDecimal(100)));
		}

		// Calculate progress.
		if (campaignStatsDTO.getAllMembers() == 0) {
			campaignStatsDTO.setProgress(BigDecimal.valueOf(0));
		} else {
			campaignStatsDTO.setProgress(
				new BigDecimal(campaignStatsDTO.getMembersContacted())
					.divide(new BigDecimal(campaignStatsDTO.getAllMembers()), 1, RoundingMode.DOWN)
					.multiply(new BigDecimal(100)));
		}

		// Calculate duration and ETA.
		long diff = 0;
		if (campaignEntity.getState() != State.TERMINATED_BY_USER
			&& campaignEntity.getState() != State.TERMINATED_BY_WORKFLOW) {
			if (campaignEntity.getStartedOn() != null) {
				diff = Instant.now().toEpochMilli() - campaignEntity.getStartedOn().toEpochMilli();
			}
		} else {
			if (campaignEntity.getTerminatedOn() != null && campaignEntity.getStartedOn() != null) {
				diff = campaignEntity.getTerminatedOn().toEpochMilli() - campaignEntity.getStartedOn()
					.toEpochMilli();
			}
		}
		campaignStatsDTO.setDuration(DurationFormatUtils.formatDurationWords(diff, true, true));

		return campaignStatsDTO;
	}

	@Transactional
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = WRITE)
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
				if (device.isEmpty()) {
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
				log.debug("Creating device monitoring entry for device with hardware id '{}' in "
					+ "campaign '{}'.", member.getIdentifier(), campaignId);
				CampaignDeviceMonitorEntity cdm = new CampaignDeviceMonitorEntity();
				cdm.setDeviceId(device.get(0).getId());
				cdm.setHardwareId(device.get(0).getHardwareId());
				cdm.setCampaignId(new ObjectId(campaignId));
				cdm.setGroup(member.getGroup());
				campaignDeviceMonitorService.save(cdm);
			} else {
				deviceResource.findByTagName(member.getIdentifier()).forEach(device -> {
					log.debug("Creating device monitoring entry for device with hardware id '{}' in "
						+ "campaign '{}'.", member.getIdentifier(), campaignId);
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

	@Transactional
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = DELETE)
	public void delete(String campaignId) {
		try {
			log.debug("Terminating campaign instance in workflow engine for campaign id '{}'.",
				campaignId);
			terminate(campaignId);
			log.debug("Terminated campaign instance in workflow engine for campaign id '{}'.",
				campaignId);
		} catch (ClientStatusException e) {
			log.warn("Could not delete campaign instance in workflow engine.", e);
		}
		log.debug("Deleting campaign with id '{}'.", campaignId);
		boolean deletedCampaigns = deleteById(campaignId);
		log.debug("Deleted campaign with id '{}' - records deleted: '{}'.", campaignId,
			deletedCampaigns);
		log.debug("Deleting campaign device monitor entries for campaign id '{}'.", campaignId);
		long deletedDeviceMonitors =
			campaignDeviceMonitorService.deleteByColumn("campaignId", new ObjectId(campaignId));
		log.debug("Deleted campaign device monitor entries for campaign id '{}' - records deleted: "
			+ "'{}'.", campaignId, deletedDeviceMonitors);
	}

	@Transactional
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = WRITE)
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
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = CREATE)
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

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = READ)
	public List<CampaignConditionDTO> getCondition(CampaignEntity campaignEntity,
		GroupDTO groupDTO, Condition.Type type) {
		return campaignEntity.getConditions().stream()
			.filter(condition -> condition.getType() == type)
			.filter(condition -> condition.getGroup() == groupDTO.getGroup())
			.filter(condition -> condition.getStage() == groupDTO.getStage())
			.toList();
	}

	@Transactional
	public CampaignEntity setStateDescription(String campaignId, String stateDescription) {
		log.debug("Setting state description for campaign id '{}' to '{}'.", campaignId,
			stateDescription);
		CampaignEntity campaignEntity = findById(campaignId);
		campaignEntity.setStateDescription(stateDescription);
		return save(campaignEntity);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = READ)
	public CampaignEntity findById(String id) {
		return super.findById(id);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = READ)
	public Page<CampaignEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CAMPAIGN, operation = READ)
	public List<CampaignEntity> getAll() {
		return super.getAll();
	}
}
