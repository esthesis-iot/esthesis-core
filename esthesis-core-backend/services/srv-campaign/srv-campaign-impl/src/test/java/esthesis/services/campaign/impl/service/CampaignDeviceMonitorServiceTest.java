package esthesis.services.campaign.impl.service;

import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.services.campaign.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static esthesis.core.common.AppConstants.Campaign.State.CREATED;
import static esthesis.core.common.AppConstants.Campaign.Type.PROVISIONING;
import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class CampaignDeviceMonitorServiceTest {

	@Inject
	CampaignDeviceMonitorService campaignDeviceMonitorService;

	@Inject
	CampaignService campaignService;

	@Inject
	TestHelper testHelper;


	@BeforeEach
	void clearDatabase() {
		testHelper.clearDatabase();
	}


	@Test
	void findByCampaignID() {
		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor for the given campaign.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId));

		// Assert campaign device monitor is found by campaign ID.
		assertNotNull(campaignDeviceMonitorService.findByCampaignID(campaignId.toHexString()));

		// Assert campaign device monitor is not found by non-existent campaign ID.
		assertTrue(campaignDeviceMonitorService.findByCampaignID(new ObjectId().toHexString()).isEmpty());
	}

	@Test
	void findByCampaignIdAndGroup() {
		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor for the given campaign and group 1.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId).setGroup(1));

		// Assert campaign device monitor is found by campaign ID and group.
		assertFalse(campaignDeviceMonitorService.findByCampaignIdAndGroup(campaignId.toHexString(), 1).isEmpty());

		// Assert campaign device monitor is not found by campaign ID and non-existing group.
		assertTrue(campaignDeviceMonitorService.findByCampaignIdAndGroup(campaignId.toHexString(), 2).isEmpty());

		// Assert campaign device monitor is not found by non-existent campaign ID.
		assertTrue(campaignDeviceMonitorService.findByCampaignIdAndGroup(new ObjectId().toHexString(), 1).isEmpty());


	}

	@Test
	void countAll() {

		// Assert count is zero for non-existent campaign ID.
		assertEquals(0, campaignDeviceMonitorService.countAll(new ObjectId().toHexString()));

		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor for the given campaign.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId));

		// Assert count is one for the given campaign ID.
		assertEquals(1, campaignDeviceMonitorService.countAll(campaignId.toHexString()));

	}


	@Test
	void countRepliesByCampaignID() {
		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor for the given campaign without replies.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId).setCommandReplyId(null));

		// Assert count is zero for the given campaign ID.
		assertEquals(0, campaignDeviceMonitorService.countReplies(campaignId.toHexString()));

		// Perform the save operation for a new campaign device monitor for the given campaign with replies.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId));

		// Assert count is one for the given campaign ID.
		assertEquals(1, campaignDeviceMonitorService.countReplies(campaignId.toHexString()));

	}

	@Test
	void countRepliesByCampaignIDAndGroup() {
		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor for the given campaign with group 1.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId).setGroup(1));

		// Assert count is zero for the given campaign ID with wrong group.
		assertEquals(0, campaignDeviceMonitorService.countReplies(campaignId.toHexString(), 2));


		// Assert count is one for the given campaign ID and group 1.
		assertEquals(1, campaignDeviceMonitorService.countReplies(campaignId.toHexString(), 1));
	}

	@Test
	void countContactedByCampaignID() {

		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor for the given campaign that has not been contacted.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId).setCommandRequestId(null));

		// Assert count is zero for the given campaign ID.
		assertEquals(0, campaignDeviceMonitorService.countContacted(campaignId.toHexString()));

		// Perform the save operation for a new campaign device monitor for the given campaign that has been contacted.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId));

		// Assert count is one for the given campaign ID.
		assertEquals(1, campaignDeviceMonitorService.countContacted(campaignId.toHexString()));

	}

	@Test
	void countContactedByCampaignIDAndGroup() {
		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor for the given campaign  with group 1.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId).setGroup(1));

		// Assert count is zero for the given campaign ID and wrong group.
		assertEquals(0, campaignDeviceMonitorService.countContacted(campaignId.toHexString(), 2));

		// Assert count is one for the given campaign ID and group 1.
		assertEquals(1, campaignDeviceMonitorService.countContacted(campaignId.toHexString(), 1));

	}

	@Test
	void countInGroup() {
		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor for the given campaign  with group 1.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId).setGroup(1));

		// Assert count is one for the given campaign ID and group 1.
		assertEquals(1, campaignDeviceMonitorService.countInGroup(campaignId.toHexString(), 1));

		// Assert count is zero for the given campaign ID and non-existing group.
		assertEquals(0, campaignDeviceMonitorService.countInGroup(campaignId.toHexString(), 2));
	}


	@Test
	void countContactedNotReplied() {
		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor that has been contacted and has replies.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId));

		// Assert count is zero for the given campaign ID.
		assertEquals(0, campaignDeviceMonitorService.countContactedNotReplied(campaignId.toHexString()));

		// Perform the save operation for a new campaign device monitor that has been contacted and has no replies.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId).setCommandReplyId(null));

		// Assert count is one for the given campaign ID.
		assertEquals(1, campaignDeviceMonitorService.countContactedNotReplied(campaignId.toHexString()));
	}

	@Test
	void findContactedNotReplied() {
		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor that has been contacted and has replies with group 1.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId).setGroup(1));

		// Assert no device monitors are found for the given campaign ID.
		assertTrue(campaignDeviceMonitorService.findContactedNotReplied(campaignId.toHexString(), 1).isEmpty());

		// Perform the save operation for a new campaign device monitor that has been contacted and has no replies for group 1.
		campaignDeviceMonitorService.save(
			testHelper.makeCampaignDeviceMonitorEntity(
					campaignId)
				.setCommandReplyId(null)
				.setGroup(1)
		);

		// Assert device monitors are found for the given campaign ID.
		assertFalse(campaignDeviceMonitorService.findContactedNotReplied(campaignId.toHexString(), 1).isEmpty());

	}

	@Test
	void findNotContacted() {
		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor that has been contacted and has replies with group 1.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId).setGroup(1));

		// Assert no device monitors not contacted are found for the given campaign ID and group 1.
		assertTrue(campaignDeviceMonitorService.findNotContacted(campaignId.toHexString(), 1, 10).isEmpty());

		// Perform the save operation for a new campaign device monitor that has not been contacted for group 1.
		campaignDeviceMonitorService.save(
			testHelper.makeCampaignDeviceMonitorEntity(
					campaignId)
				.setCommandRequestId(null)
				.setGroup(1)
		);

		// Assert device monitors not contacted are found for the given campaign ID and group 1.
		assertFalse(campaignDeviceMonitorService.findNotContacted(campaignId.toHexString(), 1, 10).isEmpty());
	}

	@Test
	void hasUncontactedDevices() {
		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor that has been contacted and has replies with group 1.
		campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity(campaignId).setGroup(1));

		// Assert has uncontacted devices is false for the given campaign ID and group 1.
		assertFalse(campaignDeviceMonitorService.hasUncontactedDevices(campaignId.toHexString(), 1));

		// Perform the save operation for a new campaign device monitor that has not been contacted for group 1.
		campaignDeviceMonitorService.save(
			testHelper.makeCampaignDeviceMonitorEntity(
					campaignId)
				.setCommandRequestId(null)
				.setGroup(1)
		);

		// Assert has uncontacted devices is true for the given campaign ID and group 1.
		assertTrue(campaignDeviceMonitorService.hasUncontactedDevices(campaignId.toHexString(), 1));
	}

	@Test
	void checkRate() {
		// Perform the save operation for a new campaign.
		ObjectId campaignId = campaignService.saveNew(
				testHelper.makeCampaignEntity(
					"test-campaign-new",
					"test description",
					PROVISIONING,
					CREATED))
			.getId();

		// Perform the save operation for a new campaign device monitor that has been contacted but has no replies with group 1.
		campaignDeviceMonitorService.save(
			testHelper.makeCampaignDeviceMonitorEntity(campaignId)
				.setCommandReplyId(null)
				.setGroup(1)
		);

		// Assert rate is zero for the given campaign ID and group 1.
		assertEquals(
			BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY),
			campaignDeviceMonitorService.checkRate(campaignId.toString(), 1)
				.setScale(2, RoundingMode.UNNECESSARY));


		// Perform the save operation for a new campaign device monitor that has been contacted and has replies with group 1.
		campaignDeviceMonitorService.save(
			testHelper.makeCampaignDeviceMonitorEntity(campaignId)
				.setGroup(1)
		);

		// Assert rate is 50% for the given campaign ID and group 1.
		assertEquals(new BigDecimal("0.50"), campaignDeviceMonitorService.checkRate(campaignId.toString(), 1));

		// Perform the save operation for a new campaign device monitor that has been contacted and has replies with group 2.
		campaignDeviceMonitorService.save(
			testHelper.makeCampaignDeviceMonitorEntity(campaignId)
				.setGroup(2)
		);

		// Assert rate is 100% for the given campaign ID and group 2.
		assertEquals(new BigDecimal("1.00"), campaignDeviceMonitorService.checkRate(campaignId.toString(), 2));

	}

	@Test
	void save() {

		// Perform the save operation for a new campaign device monitor.
		CampaignDeviceMonitorEntity campaignDeviceMonitor =
			CampaignDeviceMonitorEntity.builder()
				.campaignId(new ObjectId())
				.deviceId(new ObjectId())
				.hardwareId("test-hardware-id")
				.build();

		campaignDeviceMonitorService.save(campaignDeviceMonitor).getId().toHexString();


		// Assert the campaign device monitor ID was persisted with the expected value.
		assertEquals(
			"test-hardware-id",
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.getFirst()
				.getHardwareId());

	}

	@Test
	void deleteByColumn() {

		// Perform the save operation for a new campaign device monitor with group 1.
		CampaignDeviceMonitorEntity campaignDeviceMonitor =
			campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity().setGroup(1));

		//Assert the campaign device monitor exists.
		assertFalse(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

		// Delete the campaign device monitor by campaign ID.
		campaignDeviceMonitorService.deleteByColumn("campaignId", campaignDeviceMonitor.getCampaignId());

		// Assert the campaign device monitor no longer exists.
		assertTrue(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

		// Perform the save operation for a new campaign device monitor with group 1.
		campaignDeviceMonitor =
			campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity().setGroup(1));

		//Assert the campaign device monitor exists.
		assertFalse(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

		// Delete the campaign device monitor by device ID.
		campaignDeviceMonitorService.deleteByColumn("deviceId", campaignDeviceMonitor.getDeviceId());

		// Assert the campaign device monitor no longer exists.
		assertTrue(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

		// Perform the save operation for a new campaign device monitor with group 1.
		campaignDeviceMonitor =
			campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity().setGroup(1));

		//Assert the campaign device monitor exists.
		assertFalse(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

		// Delete the campaign device monitor by group.
		campaignDeviceMonitorService.deleteByColumn("group", campaignDeviceMonitor.getGroup());

		// Assert the campaign device monitor no longer exists.
		assertTrue(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

		// Perform the save operation for a new campaign device monitor with group 1.
		campaignDeviceMonitor =
			campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity().setGroup(1));

		//Assert the campaign device monitor exists.
		assertFalse(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

		// Delete the campaign device monitor by hardware ID.
		campaignDeviceMonitorService.deleteByColumn("hardwareId", campaignDeviceMonitor.getHardwareId());

		// Assert the campaign device monitor no longer exists.
		assertTrue(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

		// Perform the save operation for a new campaign device monitor with group 1.
		campaignDeviceMonitor =
			campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity().setGroup(1));

		//Assert the campaign device monitor exists.
		assertFalse(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

		// Delete the campaign device monitor by command reply ID.
		campaignDeviceMonitorService.deleteByColumn("commandReplyId", campaignDeviceMonitor.getCommandReplyId());

		// Assert the campaign device monitor no longer exists.
		assertTrue(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

		// Perform the save operation for a new campaign device monitor with group 1.
		campaignDeviceMonitor =
			campaignDeviceMonitorService.save(testHelper.makeCampaignDeviceMonitorEntity().setGroup(1));

		//Assert the campaign device monitor exists.
		assertFalse(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

		// Delete the campaign device monitor by command request ID.
		campaignDeviceMonitorService.deleteByColumn("commandRequestId", campaignDeviceMonitor.getCommandRequestId());

		// Assert the campaign device monitor no longer exists.
		assertTrue(
			campaignDeviceMonitorService.findByCampaignID(
					campaignDeviceMonitor
						.getCampaignId()
						.toHexString())
				.isEmpty());

	}
}
