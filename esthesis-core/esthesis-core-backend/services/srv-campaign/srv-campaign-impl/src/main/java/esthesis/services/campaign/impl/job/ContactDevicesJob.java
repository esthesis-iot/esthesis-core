package esthesis.services.campaign.impl.job;

import esthesis.avro.CommandType;
import esthesis.avro.ExecutionType;
import esthesis.common.AppConstants.Campaign.Condition.Stage;
import esthesis.common.AppConstants.Campaign.Condition.Type;
import esthesis.common.exception.QMismatchException;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.resource.CommandSystemResource;
import esthesis.services.campaign.impl.dto.GroupDTO;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import java.time.Instant;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class ContactDevicesJob implements JobHandler {

	@Inject
	CampaignService campaignService;

	@Inject
	CampaignDeviceMonitorService campaignDeviceMonitorService;

	@Inject
	@RestClient
	CommandSystemResource commandSystemResource;

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
		campaignService.setStateDescription(campaignEntity.getId().toHexString(), "Finding batch size"
			+ ".");
		Integer batchSize = null;

		// Find group-specific batch size.
		List<CampaignConditionDTO> conditions = campaignService.getCondition(campaignEntity, groupDTO
			, Type.BATCH);
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
		GroupDTO globalGroup = new GroupDTO(Stage.INSIDE, 0);
		conditions = campaignService.getCondition(campaignEntity, globalGroup, Type.BATCH);
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

	@JobWorker(type = "ContactDevicesJob")
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
		GroupDTO groupDTO = new GroupDTO(job);
		log.debug("contactDevices, campaignId: '{}', groupExpression: '{}'", p.getCampaignId(),
			groupDTO);
		// Get the campaign details and parse the group expression.
		CampaignEntity campaignEntity = campaignService.findById(p.getCampaignId());

		// Find the batch size for this group.
		int batchSize = findBatchSize(campaignEntity, groupDTO);
		log.debug("Batch size is '{}'", batchSize);

		// Find the devices to contact.
		List<CampaignDeviceMonitorEntity> devices = campaignDeviceMonitorService.findNotContacted(
			p.getCampaignId(), groupDTO.getGroup(), batchSize);
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
			client.newCompleteCommand(job.getKey()).send().join();
		}
	}

}
