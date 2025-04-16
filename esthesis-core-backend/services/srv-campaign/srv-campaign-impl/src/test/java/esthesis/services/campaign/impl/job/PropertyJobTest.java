package esthesis.services.campaign.impl.job;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Campaign.Condition.Op;
import esthesis.core.common.AppConstants.Campaign.Condition.Type;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.TestHelper;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import esthesis.services.campaign.impl.service.CampaignService;
import esthesis.util.redis.RedisUtils;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.CompleteJobResponse;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static esthesis.core.common.AppConstants.Campaign.State.CREATED;
import static esthesis.core.common.AppConstants.Campaign.Type.EXECUTE_COMMAND;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class PropertyJobTest {

	@Inject
	PropertyJob propertyJob;

	@Inject
	TestHelper testHelper;

	@Inject
	CampaignService campaignService;

	@Inject
	CampaignDeviceMonitorService campaignDeviceMonitorService;

	@InjectMock
	RedisUtils redisUtils;

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
	void handleWithoutConditions() {
		// Arrange a campaign.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity(
					"test",
					"test",
					EXECUTE_COMMAND,
					CREATED)
				.setConditions(List.of()));

		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaign.getId()));


		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		assertDoesNotThrow(() -> propertyJob.handle(jobClient, activatedJob));
		assertTrue(parameters.isPropertyCondition());

	}

	@Test
	void handlePropertyIgnorable() {
		// Arrange a campaign.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity(
				"test",
				"test",
				EXECUTE_COMMAND,
				CREATED));

		// Update the campaign to have a property condition ignorable.
		campaign.setConditions(List.of(campaign.getConditions().getFirst().setType(Type.PROPERTY).setPropertyIgnorable(true)));
		campaignService.saveUpdate(campaign);
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaign.getId()).setGroup(1));

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);

		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		// Mock redis to not return any value for the device.
		when(redisUtils.getFromHash(any(), anyString(), anyString())).thenReturn(null);

		assertDoesNotThrow(() -> propertyJob.handle(jobClient, activatedJob));
		assertTrue(parameters.isPropertyCondition());

	}

	@ParameterizedTest
	@MethodSource("trueConditionsTestCases")
	void handleTrueConditions(Op op, String conditionValue, String deviceValue) {
		// Arrange a campaign.
		CampaignEntity campaign = campaignService.saveNew(testHelper.makeCampaignEntity(
			"test",
			"test",
			EXECUTE_COMMAND,
			CREATED));

		campaign.setConditions(List.of(
			campaign.getConditions()
				.getFirst()
				.setType(Type.PROPERTY)
				.setPropertyName("test")
				.setValue(conditionValue)
				.setOperation(op)));
		campaignService.saveUpdate(campaign);
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaign.getId()).setGroup(1));

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());

		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);
		when(redisUtils.getFromHash(any(), anyString(), anyString())).thenReturn(deviceValue);

		// Act & Assert
		assertDoesNotThrow(() -> propertyJob.handle(jobClient, activatedJob));
		assertTrue(parameters.isPropertyCondition());
	}


	@ParameterizedTest
	@MethodSource("falseConditionsTestCases")
	void handleFalseConditions(Op op, String conditionValue, String deviceValue) {
		// Arrange a campaign.
		CampaignEntity campaign = campaignService.saveNew(testHelper.makeCampaignEntity(
			"test",
			"test",
			EXECUTE_COMMAND,
			CREATED));

		campaign.setConditions(List.of(
			campaign.getConditions()
				.getFirst()
				.setType(Type.PROPERTY)
				.setPropertyName("test")
				.setValue(conditionValue)
				.setOperation(op)));
		campaignService.saveUpdate(campaign);
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaign.getId()).setGroup(1));

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(AppConstants.Campaign.Condition.Stage.ENTRY.name());

		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);
		when(redisUtils.getFromHash(any(), anyString(), anyString())).thenReturn(deviceValue);

		// Act & Assert
		assertDoesNotThrow(() -> propertyJob.handle(jobClient, activatedJob));
		assertFalse(parameters.isPropertyCondition());
	}


	/**
	 * Test cases for true conditions.
	 *
	 * @return Arguments stream with operation, condition value and device value.
	 */
	private static Stream<Arguments> trueConditionsTestCases() {
		return Stream.of(
			Arguments.of(Op.LT, "10", "9"),
			Arguments.of(Op.LTE, "10", "9"),
			Arguments.of(Op.LTE, "10", "10"),
			Arguments.of(Op.EQUAL, "10", "10"),
			Arguments.of(Op.GT, "9", "10"),
			Arguments.of(Op.GTE, "10", "10"),
			Arguments.of(Op.LT, "b", "a"),
			Arguments.of(Op.LTE, "b", "a"),
			Arguments.of(Op.EQUAL, "a", "a"),
			Arguments.of(Op.GT, "a", "b")
		);
	}

	/**
	 * Test cases for false conditions.
	 *
	 * @return Arguments stream with operation, condition value and device value.
	 */
	private static Stream<Arguments> falseConditionsTestCases() {
		return Stream.of(
			Arguments.of(Op.LT, "9", "10"),
			Arguments.of(Op.LT, "10", "10"),
			Arguments.of(Op.LTE, "9", "10"),
			Arguments.of(Op.EQUAL, "11", "10"),
			Arguments.of(Op.EQUAL, "10", "11"),
			Arguments.of(Op.GT, "10", "9"),
			Arguments.of(Op.GT, "10", "10"),
			Arguments.of(Op.GTE, "10", "9"),
			Arguments.of(Op.LT, "a", "b"),
			Arguments.of(Op.LT, "a", "a"),
			Arguments.of(Op.LTE, "a", "b"),
			Arguments.of(Op.EQUAL, "b", "a"),
			Arguments.of(Op.EQUAL, "a", "b"),
			Arguments.of(Op.GT, "b", "a"),
			Arguments.of(Op.GT, "a", "a"),
			Arguments.of(Op.GTE, "b", "a")
		);
	}

}
