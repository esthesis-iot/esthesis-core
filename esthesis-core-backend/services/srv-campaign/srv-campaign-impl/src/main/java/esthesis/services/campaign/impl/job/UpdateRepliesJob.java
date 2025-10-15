package esthesis.services.campaign.impl.job;

import esthesis.core.common.entity.CommandReplyEntity;
import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.campaign.resource.CampaignSystemResource;
import esthesis.service.command.resource.CommandSystemResource;
import esthesis.services.campaign.impl.dto.GroupDTO;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

/**
 * A job handler for updating replies from devices.
 */
@Slf4j
@ApplicationScoped
public class UpdateRepliesJob implements JobHandler {

	@Inject
	CampaignDeviceMonitorService campaignDeviceMonitorService;

	@Inject
	@RestClient
	CampaignSystemResource campaignSystemResource;

	@Inject
	@RestClient
	CommandSystemResource commandSystemResource;

	/**
	 * Updates replies from devices, so that rate conditions can be properly checked.
	 *
	 * @param client The job client.
	 * @param job    The activated job.
	 */
	@JobWorker(type = "UpdateRepliesJob")
	public void handle(JobClient client, ActivatedJob job) {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
		campaignSystemResource.setStateDescription(p.getCampaignId(), "Updating replies.");
		GroupDTO groupDTO = new GroupDTO(job);
		// Before checking the rate, update any possible replies received.
		List<CampaignDeviceMonitorEntity> contactedDevices =
			campaignDeviceMonitorService.findContactedNotReplied(p.getCampaignId(),
				groupDTO.getGroup());
		contactedDevices.forEach(device -> {
			List<CommandReplyEntity> replies = commandSystemResource.getReplies(
				device.getCommandRequestId().toString());
			if (!CollectionUtils.isEmpty(replies)) {
				device.setCommandReplyId(replies.get(0).getId());
				campaignDeviceMonitorService.save(device);
				log.debug("Updating reply '{}' for device '{}'.", replies.get(0), device.getHardwareId());
			}
		});

		client.newCompleteCommand(job.getKey()).send().join();
	}
}
