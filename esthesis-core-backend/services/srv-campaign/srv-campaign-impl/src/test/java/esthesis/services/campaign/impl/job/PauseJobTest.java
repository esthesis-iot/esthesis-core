package esthesis.services.campaign.impl.job;

import esthesis.common.exception.QMismatchException;
import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Campaign.Condition.Type;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class PauseJobTest {

	@Inject
	PauseJob pauseJob;

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
	void handleTimerMinutesCondition() {
		// Arrange a campaign.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity(
				"test",
				"test",
				EXECUTE_COMMAND, CREATED));

		// Set campaign condition operation to TIMER_MINUTES with 10 minutes value.
		campaign.getConditions().getFirst().setOperation(AppConstants.Campaign.Condition.Op.TIMER_MINUTES);
		campaign.getConditions().getFirst().setValue("10");
		campaign.getConditions().getFirst().setType(Type.PAUSE);
		campaign.setConditions(List.of(campaign.getConditions().getFirst()));
		campaignService.saveUpdate(campaign);

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		assertDoesNotThrow(() -> pauseJob.handle(jobClient, activatedJob));
		assertEquals(10, parameters.getPauseCondition());

	}

	@Test
	void handleForeverCondition() {
		// Arrange a campaign.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity(
				"test",
				"test",
				EXECUTE_COMMAND, CREATED));

		// Set campaign condition operation to TIMER_MINUTES with 10 minutes value.
		campaign.getConditions().getFirst().setOperation(AppConstants.Campaign.Condition.Op.FOREVER);
		campaign.getConditions().getFirst().setType(Type.PAUSE);
		campaign.setConditions(List.of(campaign.getConditions().getFirst()));
		campaignService.saveUpdate(campaign);

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		assertDoesNotThrow(() -> pauseJob.handle(jobClient, activatedJob));
		assertEquals(0, parameters.getPauseCondition());

	}

	@Test
	void handleTimerMultipleConditions() {
		// Arrange a campaign.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity(
				"test",
				"test",
				EXECUTE_COMMAND, CREATED));

		// Update campaign to have multiple pause conditions.
		campaign.getConditions().forEach(condition -> {
			condition.setOperation(AppConstants.Campaign.Condition.Op.TIMER_MINUTES);
			condition.setValue("10");
			condition.setType(Type.PAUSE);
		});

		campaignService.saveUpdate(campaign);

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		// Assert multiple pause conditions throw exception.
		assertThrows(QMismatchException.class, () -> pauseJob.handle(jobClient, activatedJob));

	}

	@Test
	void handleWithoutCondition() {
		// Arrange a campaign.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity(
				"test",
				"test",
				EXECUTE_COMMAND, CREATED));

		// Update campaign to have no conditions.
		campaign.setConditions(List.of());
		campaignService.saveUpdate(campaign);

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		assertDoesNotThrow(() -> pauseJob.handle(jobClient, activatedJob));
		assertEquals(-1, parameters.getPauseCondition());

	}
}
