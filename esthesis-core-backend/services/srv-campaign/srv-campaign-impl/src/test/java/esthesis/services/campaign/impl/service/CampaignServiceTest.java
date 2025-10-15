package esthesis.services.campaign.impl.service;

import static esthesis.core.common.AppConstants.Campaign.Condition.Stage.ENTRY;
import static esthesis.core.common.AppConstants.Campaign.Condition.Type.SUCCESS;
import static esthesis.core.common.AppConstants.Campaign.Member.Type.DEVICE;
import static esthesis.core.common.AppConstants.Campaign.State.CREATED;
import static esthesis.core.common.AppConstants.Campaign.State.RUNNING;
import static esthesis.core.common.AppConstants.Campaign.Type.EXECUTE_COMMAND;
import static esthesis.core.common.AppConstants.Campaign.Type.PROVISIONING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import esthesis.service.campaign.dto.CampaignMemberDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.exception.CampaignDeviceAmbiguous;
import esthesis.service.campaign.exception.CampaignDeviceDoesNotExist;
import esthesis.service.device.resource.DeviceResource;
import esthesis.services.campaign.impl.TestHelper;
import esthesis.services.campaign.impl.dto.GroupDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

@QuarkusTest
class CampaignServiceTest {

	@Inject
	TestHelper testHelper;

	@Inject
	CampaignService campaignService;

	@InjectMock
	CampaignDeviceMonitorService campaignDeviceMonitorService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	DeviceResource deviceResource;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		//Clear DB
		testHelper.clearDatabase();

		// Mock monitor metrics and counters.
		when(campaignDeviceMonitorService.countInGroup(anyString(), anyInt())).thenReturn(10L);
		when(campaignDeviceMonitorService.countContacted(anyString())).thenReturn(10L);
		when(campaignDeviceMonitorService.countContacted(anyString(), anyInt())).thenReturn(10L);
		when(campaignDeviceMonitorService.countReplies(anyString())).thenReturn(10L);
		when(campaignDeviceMonitorService.countReplies(anyString(), anyInt())).thenReturn(10L);
		when(campaignDeviceMonitorService.countAll(anyString())).thenReturn(100L);

	}

	@Test
	void saveNew() {
		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId()
			.toHexString();

		// Assert campaign was saved with the correct values.
		CampaignEntity campaign = campaignService.findById(campaignId);
		assertEquals("test-campaign-new", campaign.getName());
		assertEquals("test description", campaign.getDescription());
		assertEquals(PROVISIONING, campaign.getType());
		assertEquals(CREATED, campaign.getState());

	}

	@Test
	void saveUpdate() {
		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId()
			.toHexString();

		// Perform the update operation.
		CampaignEntity campaign = campaignService.findById(campaignId);
		campaign.setName("test-campaign-updated");
		campaign.setDescription("test description updated");
		campaign.setType(EXECUTE_COMMAND);
		campaign.setState(RUNNING);
		campaignService.saveUpdate(campaign);

		// Assert campaign was update with the correct values.
		CampaignEntity updatedCampaign = campaignService.findById(campaignId);
		assertEquals("test-campaign-updated", updatedCampaign.getName());
		assertEquals("test description updated", updatedCampaign.getDescription());
		assertEquals(EXECUTE_COMMAND, updatedCampaign.getType());
		assertEquals(RUNNING, updatedCampaign.getState());

	}

	@Test
	void resume() {
		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId()
			.toHexString();

		// Assert resume operation does not throw an exception.
		assertDoesNotThrow(() -> campaignService.resume(campaignId));

		// Todo add zeebeClient verifications.
	}

	@Test
	void findGroups() {
		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId()
			.toHexString();

		// Assert groups are found.
		assertFalse(campaignService.findGroups(campaignId).isEmpty());
	}

	@Test
	void findGroupsWithOneGroup() {
		// Perform the save operation for a new campaign with one group.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED).setMembers(List.of(CampaignMemberDTO.builder()
					.group(0).type(DEVICE).identifier("test-campaign-member-0").build()))
			)
			.getId()
			.toHexString();

		// Assert groups are found.
		assertFalse(campaignService.findGroups(campaignId).isEmpty());
	}

	@Test
	void getCampaignStats() {
		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId()
			.toHexString();

		// Assert campaign stats are found.
		assertNotNull(campaignService.getCampaignStats(campaignId));

	}

	@Test
	void start() {
		// Mock finding devices by hardware ID and tag name.
		when(deviceResource.findByHardwareIds(anyString()))
			.thenReturn(List.of(testHelper.makeDeviceEntity("test-device-1")));
		when(deviceResource.findByTagName(anyString()))
			.thenReturn(List.of(testHelper.makeDeviceEntity("test-device-1")));

		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
						"test-campaign-new",
						"test description",
						PROVISIONING,
						CREATED)
					.setProcessInstanceId(null)
					.setStartedOn(null)
					.setTerminatedOn(null)
			)
			.getId()
			.toHexString();

		// Perform the start operation for the campaign.
		campaignService.start(campaignId);

		// Assert campaign has been started.
		CampaignEntity campaign = campaignService.findById(campaignId);
		assertEquals(RUNNING, campaign.getState());
		assertNotNull(campaign.getProcessInstanceId());
		assertNotNull(campaign.getStartedOn());
		assertNull(campaign.getTerminatedOn());
	}

	@Test
	void startWithoutDeviceFound() {

		// Mock finding no device.
		when(deviceResource.findByHardwareIds(anyString())).thenReturn(List.of());

		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
						"test-campaign-new",
						"test description",
						PROVISIONING,
						CREATED)
					.setProcessInstanceId(null)
					.setStartedOn(null)
					.setTerminatedOn(null)
			)
			.getId()
			.toHexString();

		// Assert starting the campaign throws an exception due to not finding any device.
		 assertThrows(CampaignDeviceDoesNotExist.class, () ->  campaignService.start(campaignId) );
	}

	@Test
	void startWithMultipleDevicesFound() {

		// Mock finding multiple devices.
		when(deviceResource.findByHardwareIds(anyString())).thenReturn(List.of(
			testHelper.makeDeviceEntity("test-device-1"),
			testHelper.makeDeviceEntity("test-device-2")));

		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
						"test-campaign-new",
						"test description",
						PROVISIONING,
						CREATED)
					.setProcessInstanceId(null)
					.setStartedOn(null)
					.setTerminatedOn(null)
			)
			.getId()
			.toHexString();

		// Assert starting the campaign throws an exception due to finding multiple devices.
		assertThrows(CampaignDeviceAmbiguous.class, () ->  campaignService.start(campaignId) );
	}

	@Test
	void delete() {
		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId()
			.toHexString();

		// Perform the delete operation for the campaign.
		campaignService.delete(campaignId);

		// Assert campaign has been deleted.
		assertNull(campaignService.findById(campaignId));

		// Todo verify device monitor resource calls.
	}

	@Test
	void replicate() {
		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId()
			.toHexString();

		// Perform the replicate operation for the campaign.
		String replicatedId = campaignService.replicate(campaignId).getId().toHexString();

		// Assert both campaign exists
		assertNotNull(campaignService.findById(campaignId));
		assertNotNull(campaignService.findById(replicatedId));

	}

	@Test
	void getCondition() {
		// Perform the save operation for a new campaign.
		CampaignEntity campaign = campaignService.saveNew(
			testHelper.makeCampaignEntity(
				"test-campaign-new",
				"test description",
				PROVISIONING,
				CREATED));

		// Assert conditions can be found for the given campaign, group and condition type.
		assertFalse(campaignService.getCondition(campaign, new GroupDTO(ENTRY, 1), SUCCESS).isEmpty());

	}

	@Test
	void setStateDescription() {
		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId()
			.toHexString();

		// Perform the update of the state description.
		campaignService.setStateDescription(campaignId, "Updated state description");
		// Assert update was persisted.
		assertEquals("Updated state description",
			campaignService.findById(campaignId).getStateDescription());
	}

	@Test
	void findById() {
		// Perform the save operation for a new campaign.
		String campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId()
			.toHexString();

		// Assert campaign was found.
		assertNotNull(campaignService.findById(campaignId));
	}

	@Test
	void find() {

		// Assert no campaign is found.
		assertTrue(
			campaignService.find(
					testHelper.makePageable(0, 10))
				.getContent()
				.isEmpty());

		// Perform the save operation for a new campaign.
		campaignService.saveNew(
			testHelper.makeCampaignEntity(
				"test-campaign-new",
				"test description",
				PROVISIONING,
				CREATED));

		// Assert campaign is found.
		assertFalse(
			campaignService.find(
					testHelper.makePageable(0, 10))
				.getContent()
				.isEmpty());
	}
}
