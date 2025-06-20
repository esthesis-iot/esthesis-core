package esthesis.services.campaign.impl.job;

import esthesis.core.common.AppConstants;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.command.entity.CommandReplyEntity;
import esthesis.service.command.resource.CommandSystemResource;
import esthesis.services.campaign.impl.TestHelper;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
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
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static esthesis.core.common.AppConstants.Campaign.State.CREATED;
import static esthesis.core.common.AppConstants.Campaign.Type.EXECUTE_COMMAND;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class UpdateRepliesJobTest {

	@Inject
	UpdateRepliesJob updateRepliesJob;

	@Inject
	TestHelper testHelper;

	@Inject
	CampaignService campaignService;

	@Inject
	CampaignDeviceMonitorService campaignDeviceMonitorService;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	CommandSystemResource commandSystemResource;

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
		// Arrange a campaign and  a campaign device monitor.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity("test", "test", EXECUTE_COMMAND, CREATED));
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(
			campaign.getId()).setGroup(1).setCommandReplyId(null));

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		// Mock command replies.
		CommandReplyEntity commandReply = new CommandReplyEntity("correlation-id",
			"hardware-id",
			true,
			"test-output",
			Instant.now().minus(1, ChronoUnit.MINUTES),
			false);
		commandReply.setId(new ObjectId());
		when(commandSystemResource.getReplies(anyString())).thenReturn(List.of(commandReply));

		assertDoesNotThrow(() -> updateRepliesJob.handle(jobClient, activatedJob));

		verify(commandSystemResource).getReplies(anyString());

		// Assert that the command reply ID is set in the device monitor.
		assertNotNull(campaignDeviceMonitorService.findByCampaignID(campaign.getId().toHexString()).getFirst().getCommandReplyId());
	}
}
