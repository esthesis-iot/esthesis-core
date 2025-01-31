package esthesis.services.campaign.impl.service;

import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.services.campaign.impl.TestHelper;
import esthesis.services.campaign.impl.dto.GroupDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static esthesis.core.common.AppConstants.Campaign.Condition.Stage.ENTRY;
import static esthesis.core.common.AppConstants.Campaign.Condition.Type.SUCCESS;
import static esthesis.core.common.AppConstants.Campaign.State.CREATED;
import static esthesis.core.common.AppConstants.Campaign.State.RUNNING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

		// Setup initial campaign entities
		List<CampaignEntity> campaignEntities =
			List.of(
				testHelper.makeCampaignEntity().setName("test-campaign-1"),
				testHelper.makeCampaignEntity().setName("test-campaign-2"),
				testHelper.makeCampaignEntity().setName("test-campaign-3"),
				testHelper.makeCampaignEntity().setName("test-campaign-4"),
				testHelper.makeCampaignEntity().setName("test-campaign-5")
			);

		campaignEntities.forEach(testHelper::persistCampaignEntity);

		// Mock metrics service methods
		when(campaignDeviceMonitorService.countInGroup(anyString(), anyInt())).thenReturn(10L);
		when(campaignDeviceMonitorService.countContacted(anyString())).thenReturn(10L);
		when(campaignDeviceMonitorService.countContacted(anyString(), anyInt())).thenReturn(10L);
		when(campaignDeviceMonitorService.countReplies(anyString())).thenReturn(10L);
		when(campaignDeviceMonitorService.countReplies(anyString(), anyInt())).thenReturn(10L);
		when(campaignDeviceMonitorService.countAll(anyString())).thenReturn(100L);

		// Mock device resource
		when(deviceResource.findByHardwareIds(anyString(), anyBoolean()))
			.thenReturn(List.of(testHelper.makeDeviceEntity("test-device-1")));
		when(deviceResource.findByTagName(anyString()))
			.thenReturn(List.of(testHelper.makeDeviceEntity("test-device-2")));

	}

	@Test
	void saveNew() {
		long initialCount = testHelper.findAllCampaignEntities().stream().count();

		CampaignEntity campaign = testHelper.makeCampaignEntity().setName("test-campaign-new");
		campaignService.saveNew(campaign);

		List<CampaignEntity> allCampaign = testHelper.findAllCampaignEntities().list();

		assertEquals(initialCount + 1, allCampaign.size());
		assertTrue(allCampaign.stream().anyMatch(campaignEntity -> campaignEntity.getName().equals("test-campaign-new")));
	}

	@Test
	void saveUpdate() {
		long initialCount = testHelper.findAllCampaignEntities().stream().count();

		CampaignEntity campaign = testHelper.makeCampaignEntity().setName("test-campaign-new");
		campaign = campaignService.saveUpdate(campaign); // Insert
		campaign.setName("test-campaign-updated");
		campaignService.saveUpdate(campaign); // Update

		List<CampaignEntity> allCampaign = testHelper.findAllCampaignEntities().list();

		assertEquals(initialCount + 1, allCampaign.size());
		assertTrue(allCampaign.stream().anyMatch(campaignEntity -> campaignEntity.getName().equals("test-campaign-updated")));
	}

	@Test
	void resume() {
		String campaignId = new ObjectId().toString();
		assertDoesNotThrow(() -> campaignService.resume(campaignId));
	}

	@Test
	void findGroups() {
		String campaignId = testHelper.findAllCampaignEntities().stream().findFirst().orElseThrow().getId().toString();
		assertFalse(campaignService.findGroups(campaignId).isEmpty());
	}

	@Test
	void getCampaignStats() {
		String campaignId = testHelper.findAllCampaignEntities().stream().findFirst().orElseThrow().getId().toString();
		CampaignStatsDTO statsDTO = campaignService.getCampaignStats(campaignId);

		assertEquals(100L, statsDTO.getAllMembers());
		assertEquals("1 day", statsDTO.getDuration());
		assertEquals(10L, statsDTO.getMembersContacted());
		assertEquals(0L, statsDTO.getMembersContactedButNotReplied());
		assertEquals(10L, statsDTO.getMembersReplied());
		assertEquals(new BigDecimal("10.0"), statsDTO.getSuccessRate());
		assertEquals(new BigDecimal("10.0"), statsDTO.getProgress());
	}

	@Test
	void start() {
		String campaignId =
			testHelper.persistCampaignEntity(
					testHelper.makeCampaignEntity()
						.setName("test-campaign-start").setState(CREATED).setProcessInstanceId(null)
						.setStartedOn(null)
						.setTerminatedOn(null)
				)
				.getId()
				.toString();

		campaignService.start(campaignId);

		CampaignEntity campaign =
			testHelper.findAllCampaignEntities()
				.stream()
				.filter(campaignEntity -> campaignEntity.getName().equals("test-campaign-start"))
				.findFirst()
				.orElseThrow();


		assertEquals(RUNNING, campaign.getState());
		assertNotNull(campaign.getProcessInstanceId());
		assertNotNull(campaign.getStartedOn());
		assertNull(campaign.getTerminatedOn());

		// Verify if the 4 CampaignMemberDTO created from the testHelper  class had their metrics saved
		verify(campaignDeviceMonitorService, times(4)).save(any(CampaignDeviceMonitorEntity.class));
	}

	@Test
	void delete() {
		String campaignId = testHelper.findAllCampaignEntities().stream().findFirst().orElseThrow().getId().toString();
		assertDoesNotThrow(() -> campaignService.delete(campaignId));
		assertEquals(4, testHelper.findAllCampaignEntities().list().size());
	}

	@Test
	void replicate() {
		String campaignId = testHelper.findAllCampaignEntities().stream().findFirst().orElseThrow().getId().toString();
		assertDoesNotThrow(() -> campaignService.replicate(campaignId));
		assertEquals(6, testHelper.findAllCampaignEntities().list().size());
	}

	@Test
	void getCondition() {
		CampaignEntity campaign = testHelper.findAllCampaignEntities().stream().findFirst().orElseThrow();
		List<CampaignConditionDTO> conditions = campaignService.getCondition(campaign, new GroupDTO(ENTRY, 1), SUCCESS);
		assertEquals(1, conditions.size());
	}

	@Test
	void setStateDescription() {
		String campaignId = testHelper.findAllCampaignEntities().stream().findFirst().orElseThrow().getId().toString();
		campaignService.setStateDescription(campaignId, "Updated state description");

		CampaignEntity campaign = testHelper.findCampaign(campaignId);

		assertEquals("Updated state description", campaign.getStateDescription());
	}

	@Test
	void findById() {
		String campaignId = testHelper.findAllCampaignEntities().stream().findFirst().orElseThrow().getId().toString();
		CampaignEntity campaign = campaignService.findById(campaignId);
		assertNotNull(campaign);
	}

	@Test
	void find() {
		List<CampaignEntity> campaigns =
			campaignService.find(testHelper.makePageable(0, 10), true).getContent();
		assertEquals(5, campaigns.size());
	}
}
