package esthesis.services.campaign.impl.job;

import esthesis.core.common.AppConstants;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.resource.CampaignSystemResource;
import esthesis.services.campaign.impl.TestHelper;
import esthesis.services.campaign.impl.service.CampaignService;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.CompleteJobResponse;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static esthesis.core.common.AppConstants.Campaign.State.CREATED;
import static esthesis.core.common.AppConstants.Campaign.State.PAUSED_BY_WORKFLOW;
import static esthesis.core.common.AppConstants.Campaign.Type.EXECUTE_COMMAND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class SetWorkflowStatusToPausedJobTest {

	@Inject
	SetWorkflowStatusToPausedJob setWorkflowStatusToPausedJob;

	@Inject
	TestHelper testHelper;

	@Inject
	CampaignService campaignService;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	CampaignSystemResource campaignSystemResource;

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
	void handle() {
		// Arrange a campaign.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity("test", "test", EXECUTE_COMMAND, CREATED));

		// Prepare mocks for campaign system resource.
		when(campaignSystemResource.findById(campaign.getId().toHexString())).thenReturn(campaign);
		doNothing().when(campaignSystemResource).save(any(CampaignEntity.class));



		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		// Assert campaign state is CREATED.
		assertEquals(CREATED, campaignService.findById(campaign.getId().toHexString()).getState());

		assertDoesNotThrow(() -> setWorkflowStatusToPausedJob.handle(jobClient, activatedJob));

		// Verify that the campaign was saved with its state changed to 'PAUSED_BY_WORKFLOW'.
		ArgumentCaptor<CampaignEntity> campaignArgumentCaptor = ArgumentCaptor.forClass(CampaignEntity.class);
		verify(campaignSystemResource, org.mockito.Mockito.times(1)).save(campaignArgumentCaptor.capture());
	  assertEquals(PAUSED_BY_WORKFLOW, campaignArgumentCaptor.getValue().getState());


	}
}
