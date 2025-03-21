package esthesis.services.campaign.impl.job;

import esthesis.services.campaign.impl.dto.GroupDTO;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.JobWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * A job handler that checks the remaining devices condition for a campaign.
 */
@Slf4j
@ApplicationScoped
public class CheckRemainingDevicesJob implements JobHandler {

	@Inject
	CampaignDeviceMonitorService campaignDeviceMonitorService;

	@Inject
	CampaignService campaignService;

	/**
	 * Check if there are any remaining devices to contact.
	 *
	 * @param client the job client to use.
	 * @param job    the job to handle.
	 */
	@JobWorker(type = "CheckRemainingDevicesJob")
	public void handle(JobClient client, ActivatedJob job) {
		WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);
		GroupDTO groupDTO = new GroupDTO(job);
		log.debug("checkRemainingDevice, campaignId: {}, group: {}", p.getCampaignId(), groupDTO);
		campaignService.setStateDescription(p.getCampaignId(), "Checking remaining devices condition.");
		p.setRemainingDevicesCondition(
			campaignDeviceMonitorService.hasUncontactedDevices(p.getCampaignId(), groupDTO.getGroup()));
		log.debug("Remaining devices condition is '{}'.", p.isRemainingDevicesCondition());

		client.newCompleteCommand(job.getKey()).variables(p).send().join();
	}
}
