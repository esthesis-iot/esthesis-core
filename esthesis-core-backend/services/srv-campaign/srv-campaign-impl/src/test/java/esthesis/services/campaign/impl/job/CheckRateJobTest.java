package esthesis.services.campaign.impl.job;

import esthesis.core.common.AppConstants;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.TestHelper;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.CompleteJobResponse;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static esthesis.core.common.AppConstants.Campaign.State.CREATED;
import static esthesis.core.common.AppConstants.Campaign.Type.EXECUTE_COMMAND;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@QuarkusTest
class CheckRateJobTest {

	@Inject
	CheckRateJob checkRateJob;

	@Inject
	TestHelper testHelper;

	@Inject
	CampaignService campaignService;

	@Inject
	CampaignDeviceMonitorService campaignDeviceMonitorService;

	JobClient jobClient;

	ActivatedJob activatedJob;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		testHelper.clearDatabase();
		jobClient = mock(JobClient.class);
		activatedJob = mock(ActivatedJob.class);

		// Mocks for the zeebe client requests and responses.
		ZeebeFuture<CompleteJobResponse> zeebeFuture = mock(ZeebeFuture.class);
		CompleteJobCommandStep1 completeJobCommandStep1 = mock(CompleteJobCommandStep1.class);
		when(jobClient.newCompleteCommand(anyLong())).thenReturn(completeJobCommandStep1);
		when(completeJobCommandStep1.variables(any(WorkflowParameters.class))).thenReturn(completeJobCommandStep1);
		when(completeJobCommandStep1.send()).thenReturn(zeebeFuture);
		when(zeebeFuture.join()).thenReturn(mock(CompleteJobResponse.class));

	}

	@Test
	void handleOneCondition() {
		// Arrange a campaign and a campaign device monitor.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity("test", "test", EXECUTE_COMMAND, CREATED));
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaign.getId()));

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);


		// Assert that the job handler does not throw an exception and that the rate condition is set correctly.
		assertDoesNotThrow(() -> checkRateJob.handle(jobClient, activatedJob));
	}

	@Test
	void handleMultipleCondition() {
		// Arrange campaign with multiple conditions and  campaign device monitor.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity("test", "test", EXECUTE_COMMAND, CREATED));
		campaign.getConditions().add(testHelper.makeSuccessCondition());
		campaignService.saveUpdate(campaign);
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaign.getId()));

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);


		// Assert that the job handler does not throw an exception and that the rate condition is set correctly.
		assertDoesNotThrow(() -> checkRateJob.handle(jobClient, activatedJob));
	}

	@Test
	void handleNoneCondition() {
		// Arrange campaign without conditions.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity(
				"test",
				"test",
				EXECUTE_COMMAND,
				CREATED)
				.setConditions(List.of())
			);


		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);


		// Assert that the job handler does not throw an exception and that the rate condition is set correctly.
		assertDoesNotThrow(() -> checkRateJob.handle(jobClient, activatedJob));
		assertTrue(parameters.isRateCondition());
	}
}
