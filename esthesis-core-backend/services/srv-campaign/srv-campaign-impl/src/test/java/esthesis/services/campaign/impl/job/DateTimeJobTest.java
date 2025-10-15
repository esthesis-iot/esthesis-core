package esthesis.services.campaign.impl.job;

import esthesis.core.common.AppConstants.Campaign.Condition;
import esthesis.core.common.AppConstants.Campaign.Condition.Op;
import esthesis.core.common.AppConstants.Campaign.Condition.Stage;
import esthesis.service.campaign.dto.CampaignConditionDTO;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static esthesis.core.common.AppConstants.Campaign.State.CREATED;
import static esthesis.core.common.AppConstants.Campaign.Type.EXECUTE_COMMAND;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
	void handleWithoutDatetimeCondition() {
		// Arrange a campaign without conditions.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity(
					"test",
					"test",
					EXECUTE_COMMAND,
					CREATED)
				.setConditions(List.of()));

		// Mock the campaign system resource to return the campaign when requested.
		when(campaignSystemResource.findById(campaign.getId().toHexString())).thenReturn(campaign);
		when(campaignSystemResource.setStateDescription(any(), any())).thenReturn(campaign);

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		assertDoesNotThrow(() -> dateTimeJob.handle(jobClient, activatedJob));
		assertTrue(parameters.isDateTimeCondition());
	}

	@ParameterizedTest
	@MethodSource("handleUseCases")
	void handle(Op operation, Instant scheduleDate, boolean expectedResult) {
		// Arrange condition for testing.
		CampaignConditionDTO condition = new CampaignConditionDTO();
		condition.setType(Condition.Type.DATETIME);
		condition.setGroup(1);
		condition.setStage(Stage.ENTRY);
		condition.setOperation(operation);
		condition.setScheduleDate(scheduleDate);
		// Arrange a campaign.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity(
				"test", "test", EXECUTE_COMMAND, CREATED).setConditions(List.of(condition)));

		// Mock the campaign system resource to return the campaign when requested.
		when(campaignSystemResource.findById(campaign.getId().toHexString())).thenReturn(campaign);
		when(campaignSystemResource.setStateDescription(any(), any())).thenReturn(campaign);
		when(campaignSystemResource.getCondition(
			campaign.getId().toHexString(), 1, Stage.ENTRY, Condition.Type.DATETIME))
			.thenReturn(List.of(condition));

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		assertDoesNotThrow(() -> dateTimeJob.handle(jobClient, activatedJob));
		assertEquals(expectedResult, parameters.isDateTimeCondition());
	}

	static Stream<Arguments> handleUseCases() {

		Instant pastInstant = Instant.now().minus(1, ChronoUnit.HOURS);
		Instant futureInstant = Instant.now().plus(1, ChronoUnit.HOURS);

		// Arguments: operation, scheduleDate, expectedResult.
		return Stream.of(
			Arguments.of(Op.BEFORE, pastInstant, false),
			Arguments.of(Op.BEFORE, futureInstant, true),
			Arguments.of(Op.AFTER, pastInstant, true),
			Arguments.of(Op.AFTER, futureInstant, false),
			Arguments.of(Op.EQUAL, pastInstant, true),
			Arguments.of(Op.EQUAL, futureInstant, true)
		);
	}
}
