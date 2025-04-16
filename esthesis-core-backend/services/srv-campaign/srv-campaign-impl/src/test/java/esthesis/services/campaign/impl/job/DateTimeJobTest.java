package esthesis.services.campaign.impl.job;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Campaign.Condition.Op;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.TestHelper;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class DateTimeJobTest {

	@Inject
	DateTimeJob dateTimeJob;

	@Inject
	TestHelper testHelper;

	@Inject
	CampaignService campaignService;

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
	void handleWithoutDatetimeCondition() {
		// Arrange a campaign without conditions.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity(
				"test",
				"test",
				EXECUTE_COMMAND,
				CREATED)
				.setConditions(List.of()));

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		assertDoesNotThrow(() -> dateTimeJob.handle(jobClient, activatedJob));
		assertTrue(parameters.isDateTimeCondition());
	}

	@Test
	void handle() {
		// Arrange a campaign.
		CampaignEntity campaign =
			campaignService.saveNew(
				testHelper.makeCampaignEntity("test", "test", EXECUTE_COMMAND, CREATED));

		// Update the campaign with Datetime conditions.
		campaign.getConditions().add(testHelper.makeDateTimeCondition().setOperation(Op.BEFORE));
		campaign.getConditions().add(testHelper.makeDateTimeCondition().setOperation(Op.AFTER));
		campaign.getConditions().add(testHelper.makeDateTimeCondition().setOperation(Op.ABOVE));
		campaignService.saveUpdate(campaign);

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		assertDoesNotThrow(() -> dateTimeJob.handle(jobClient, activatedJob));
		assertTrue(parameters.isDateTimeCondition());
	}
}
