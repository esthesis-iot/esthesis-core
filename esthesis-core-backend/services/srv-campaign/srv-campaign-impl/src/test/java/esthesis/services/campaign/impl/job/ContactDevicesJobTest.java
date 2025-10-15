package esthesis.services.campaign.impl.job;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Campaign;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.resource.CampaignSystemResource;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static esthesis.core.common.AppConstants.Campaign.State.CREATED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ContactDevicesJobTest {

	@Inject
	ContactDevicesJob contactDevicesJob;

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

	@ParameterizedTest
	@MethodSource("provideAllCampaignTypesWithConditions")
	void handleTypes(Campaign.Type campaignType, List<CampaignConditionDTO> conditions) {
		// Arrange a campaign and a campaign device monitor that has not been contacted yet.
		CampaignEntity campaign =
			campaignService.saveNew(testHelper.makeCampaignEntity("test", "test", campaignType, CREATED)
				.setConditions(conditions));

		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaign.getId())
			.setCommandRequestId(null)
			.setGroup(1));

		// Mock campaign system resource requests.
		when(campaignSystemResource.findById(campaign.getId().toHexString())).thenReturn(campaign);

		// Prepare mocks for activated job.
		WorkflowParameters parameters = new WorkflowParameters();
		parameters.setCampaignId(campaign.getId().toHexString());
		parameters.setGroup(1);
		parameters.setStage(Campaign.Condition.Stage.ENTRY.name());
		when(activatedJob.getVariablesAsType(WorkflowParameters.class)).thenReturn(parameters);
		when(activatedJob.getKey()).thenReturn(1L);

		// Mock command system resource to return a schedule info DTO.
		when(commandSystemResource.save(any())).thenReturn(
			new ExecuteRequestScheduleInfoDTO(1, 1, new ObjectId().toHexString()));


		assertDoesNotThrow(() -> contactDevicesJob.handle(jobClient, activatedJob));

		// Verify that the command system resource was called to save the command.
		verify(commandSystemResource).save(any());
	}


	static Stream<Arguments> provideAllCampaignTypesWithConditions() {
		CampaignConditionDTO groupCondition = new CampaignConditionDTO()
			.setType(Campaign.Condition.Type.BATCH)
			.setGroup(1)
			.setStage(Campaign.Condition.Stage.ENTRY)
			.setValue("10");

		CampaignConditionDTO globalCondition = new CampaignConditionDTO()
			.setType(Campaign.Condition.Type.BATCH)
			.setGroup(0)
			.setStage(Campaign.Condition.Stage.INSIDE)
			.setValue("100");

		return Arrays.stream(AppConstants.Campaign.Type.values())
			.flatMap(type -> Stream.of(
				Arguments.of(type, List.of()),
				Arguments.of(type, List.of(groupCondition)),
				Arguments.of(type, List.of(groupCondition, groupCondition)),
				Arguments.of(type, List.of(globalCondition)),
				Arguments.of(type, List.of(globalCondition, globalCondition)),
				Arguments.of(type, List.of(groupCondition, globalCondition)),
				Arguments.of(type, List.of(groupCondition, globalCondition, groupCondition, globalCondition))
			));
	}
}
