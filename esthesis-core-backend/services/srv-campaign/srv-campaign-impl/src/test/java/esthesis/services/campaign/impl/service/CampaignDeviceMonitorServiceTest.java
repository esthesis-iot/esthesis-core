package esthesis.services.campaign.impl.service;

import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.services.campaign.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

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
	TestHelper testHelper;


	@BeforeEach
	void clearDatabase(){
		testHelper.clearDatabase();
	}


	@Test
	void findByCampaignID() {
		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create and persist an entity with the specific campaign ID
		CampaignDeviceMonitorEntity newEntity = testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID);
		testHelper.persistCampaignDeviceMonitorEntity(newEntity);

		// Persist another entity with a different campaign ID
		testHelper.persistCampaignDeviceMonitorEntity();

		// Retrieve entities by campaign ID and verify that only one is returned
		List<CampaignDeviceMonitorEntity> deviceMonitor = campaignDeviceMonitorService.findByCampaignID(campaignID.toString());
		assertEquals(1, deviceMonitor.size());
	}

	@Test
	void findByCampaignIdAndGroup() {
		// Create a new campaign ID and specify a group
		ObjectId campaignID = new ObjectId();
		int group = 2;

		// Create and persist an entity with the campaign ID and group
		CampaignDeviceMonitorEntity newEntity = testHelper.makeCampaignDeviceMonitorEntity()
			.setCampaignId(campaignID)
			.setGroup(group);
		testHelper.persistCampaignDeviceMonitorEntity(newEntity);

		// Persist another entity with a different campaign ID and group
		CampaignDeviceMonitorEntity anotherEntity = testHelper.makeCampaignDeviceMonitorEntity()
			.setCampaignId(new ObjectId())
			.setGroup(3);
		testHelper.persistCampaignDeviceMonitorEntity(anotherEntity);

		// Retrieve entities by campaign ID and group, and verify that only one is returned
		List<CampaignDeviceMonitorEntity> deviceMonitor = campaignDeviceMonitorService.findByCampaignIdAndGroup(campaignID.toString(), group);
		assertEquals(1, deviceMonitor.size());
	}

	@Test
	void countAll() {

		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create two entities with the same campaign ID
		CampaignDeviceMonitorEntity entity1 = testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID);
		CampaignDeviceMonitorEntity entity2 = testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID);

		// Persist both entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);

		// Count entities for the provided campaign ID and verify that there are two
		assertEquals(2, campaignDeviceMonitorService.countAll(campaignID.toString()));
	}


	@Test
	void countRepliesByCampaignID() {
		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create two entities with the same campaign ID with both having command replies
		CampaignDeviceMonitorEntity entity1 = testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID);
		CampaignDeviceMonitorEntity entity2 = testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID);

		// Create two entities with the same campaign ID without command replies
		CampaignDeviceMonitorEntity entity3 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setCommandReplyId(null);
		CampaignDeviceMonitorEntity entity4 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setCommandReplyId(null);

		// Persist all entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);
		testHelper.persistCampaignDeviceMonitorEntity(entity3);
		testHelper.persistCampaignDeviceMonitorEntity(entity4);

		// Count entities for the provided campaign ID and verify that two have replies
		assertEquals(2, campaignDeviceMonitorService.countReplies(campaignID.toString()));

	}

	@Test
	void countRepliesByCampaignIDAndGroup() {
		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create two entities with the same campaign ID, in different groups, both having command replies
		CampaignDeviceMonitorEntity entity1 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(1);
		CampaignDeviceMonitorEntity entity2 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(2);

		// Create two entities with the same campaign ID, in different groups, without command replies
		CampaignDeviceMonitorEntity entity3 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(1).setCommandReplyId(null);
		CampaignDeviceMonitorEntity entity4 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(2).setCommandReplyId(null);

		// Persist all entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);
		testHelper.persistCampaignDeviceMonitorEntity(entity3);
		testHelper.persistCampaignDeviceMonitorEntity(entity4);

		// Count entities in group 1 for the provided campaign ID and verify that only one has replies
		assertEquals(1, campaignDeviceMonitorService.countReplies(campaignID.toString(), 1));
	}

	@Test
	void countContactedByCampaignID() {

		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create two entities with the same campaign ID with both having been contacted
		CampaignDeviceMonitorEntity entity1 = testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID);
		CampaignDeviceMonitorEntity entity2 = testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID);

		// Create two entities with the same campaign ID without having been contacted
		CampaignDeviceMonitorEntity entity3 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setCommandRequestId(null);
		CampaignDeviceMonitorEntity entity4 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setCommandRequestId(null);

		// Persist all entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);
		testHelper.persistCampaignDeviceMonitorEntity(entity3);
		testHelper.persistCampaignDeviceMonitorEntity(entity4);

		// Count entities for the provided campaign ID and verify that two have been contacted
		assertEquals(2, campaignDeviceMonitorService.countContacted(campaignID.toString()));

	}

	@Test
	void countContactedByCampaignIDAndGroup() {
		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create two entities with the same campaign ID, in different groups, both having been contacted
		CampaignDeviceMonitorEntity entity1 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(1);
		CampaignDeviceMonitorEntity entity2 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(2);

		// Create two entities with the same campaign ID, in different groups, without having been contacted
		CampaignDeviceMonitorEntity entity3 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(1).setCommandRequestId(null);
		CampaignDeviceMonitorEntity entity4 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(2).setCommandRequestId(null);

		// Persist all entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);
		testHelper.persistCampaignDeviceMonitorEntity(entity3);
		testHelper.persistCampaignDeviceMonitorEntity(entity4);

		// Count entities in group 1 for the provided campaign ID and verify that only one has been contacted
		assertEquals(1, campaignDeviceMonitorService.countReplies(campaignID.toString(), 1));

	}

	@Test
	void countInGroup() {
		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create four entities with the same campaign ID, distributed in two groups
		CampaignDeviceMonitorEntity entity1 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(1);
		CampaignDeviceMonitorEntity entity2 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(2);
		CampaignDeviceMonitorEntity entity3 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(1);
		CampaignDeviceMonitorEntity entity4 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(2);

		// Persist all entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);
		testHelper.persistCampaignDeviceMonitorEntity(entity3);
		testHelper.persistCampaignDeviceMonitorEntity(entity4);

		// Verify that only two entities exist in group 1 for the given campaign ID
		assertEquals(2, campaignDeviceMonitorService.countReplies(campaignID.toString(), 1));
	}


	@Test
	void countContactedNotReplied() {
		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create two entities with the same campaign ID with both having been contacted and having reply
		CampaignDeviceMonitorEntity entity1 = testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID);
		CampaignDeviceMonitorEntity entity2 = testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID);

		// Create one entity with the same campaign ID having been contacted but without replies
		CampaignDeviceMonitorEntity entity3 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setCommandReplyId(null);

		// Create one entity with the same campaign ID having not been contacted neither replied
		CampaignDeviceMonitorEntity entity4 =
			testHelper.makeCampaignDeviceMonitorEntity()
				.setCampaignId(campaignID).setCommandRequestId(null).setCommandReplyId(null);

		// Persist all entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);
		testHelper.persistCampaignDeviceMonitorEntity(entity3);
		testHelper.persistCampaignDeviceMonitorEntity(entity4);

		// Count entities for the provided campaign ID and verify that only one have been contacted but not replied
		assertEquals(1, campaignDeviceMonitorService.countContactedNotReplied(campaignID.toString()));
	}

	@Test
	void findContactedNotReplied() {
		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create two entities with the same campaign ID, in different groups, with both having been contacted and having reply
		CampaignDeviceMonitorEntity entity1 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(1);
		CampaignDeviceMonitorEntity entity2 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(2);

		// Create one entity with the same campaign ID, in group 1, having been contacted but without replies
		CampaignDeviceMonitorEntity entity3 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setCommandReplyId(null).setGroup(1);

		// Create one entity with the same campaign ID, in group 1, having not been contacted neither having any replies
		CampaignDeviceMonitorEntity entity4 =
			testHelper.makeCampaignDeviceMonitorEntity()
				.setCampaignId(campaignID).setCommandRequestId(null).setCommandReplyId(null).setGroup(1);

		// Persist all entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);
		testHelper.persistCampaignDeviceMonitorEntity(entity3);
		testHelper.persistCampaignDeviceMonitorEntity(entity4);

		// Find entities for the provided campaign ID and verify that only one have been contacted and also have replies in group 1
		assertEquals(1, campaignDeviceMonitorService.findContactedNotReplied(campaignID.toString(), 1).size());

	}

	@Test
	void findNotContacted() {
		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create two entities with the same campaign ID, in different groups, with both having been contacted and having reply
		CampaignDeviceMonitorEntity entity1 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(1);
		CampaignDeviceMonitorEntity entity2 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(2);

		// Create one entity with the same campaign ID, in group 1, having been contacted but without replies
		CampaignDeviceMonitorEntity entity3 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setCommandReplyId(null).setGroup(1);

		// Create one entity with the same campaign ID, in group 1, having not been contacted neither having any replies
		CampaignDeviceMonitorEntity entity4 =
			testHelper.makeCampaignDeviceMonitorEntity()
				.setCampaignId(campaignID).setCommandRequestId(null).setCommandReplyId(null).setGroup(1);

		// Persist all entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);
		testHelper.persistCampaignDeviceMonitorEntity(entity3);
		testHelper.persistCampaignDeviceMonitorEntity(entity4);

		// Find entities for the provided campaign ID and verify that only one have not been contacted in group 1
		assertEquals(1, campaignDeviceMonitorService.findNotContacted(campaignID.toString(), 1, 10).size());
	}

	@Test
	void hasUncontactedDevices() {
		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create two entities with the same campaign ID, in different groups, with both having been contacted and having reply
		CampaignDeviceMonitorEntity entity1 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(1);
		CampaignDeviceMonitorEntity entity2 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(2);

		// Create one entity with the same campaign ID, in group 1, having not been contacted
		CampaignDeviceMonitorEntity entity3 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setCommandRequestId(null).setGroup(1);

		// Persist all entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);
		testHelper.persistCampaignDeviceMonitorEntity(entity3);

		// Verify that for the given campaign ID, there is at least one device in group 1 that has not been contacted
		assertTrue(campaignDeviceMonitorService.hasUncontactedDevices(campaignID.toString(), 1));

		// Verify that for the given campaign ID, there are no uncontacted devices in group 2
		assertFalse(campaignDeviceMonitorService.hasUncontactedDevices(campaignID.toString(), 2));
	}

	@Test
	void checkRate() {
		// Create a new campaign ID
		ObjectId campaignID = new ObjectId();

		// Create two entities with the same campaign ID, in group 1, with both having been contacted and having reply
		CampaignDeviceMonitorEntity entity1 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(1);
		CampaignDeviceMonitorEntity entity2 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(1);

		// Create one entity with the same campaign ID, in group 1, having no reply
		CampaignDeviceMonitorEntity entity3 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setCommandReplyId(null).setGroup(1);

		// Create two entities with the same campaign ID, in group 2, with both having been contacted and having reply
		CampaignDeviceMonitorEntity entity4 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(2);
		CampaignDeviceMonitorEntity entity5 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setGroup(2);

		// Create one entity with the same campaign ID, in group 2, having not been contacted
		CampaignDeviceMonitorEntity entity6 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setCommandRequestId(null).setGroup(2);

		// Create one entity with the same campaign ID, in group 3, having not been contacted
		CampaignDeviceMonitorEntity entity7 =
			testHelper.makeCampaignDeviceMonitorEntity().setCampaignId(campaignID).setCommandRequestId(null).setGroup(2);

		// Persist all entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);
		testHelper.persistCampaignDeviceMonitorEntity(entity3);
		testHelper.persistCampaignDeviceMonitorEntity(entity4);
		testHelper.persistCampaignDeviceMonitorEntity(entity5);
		testHelper.persistCampaignDeviceMonitorEntity(entity6);
		testHelper.persistCampaignDeviceMonitorEntity(entity7);

		// For the given campaign ID, verify a 0% rate for group 3
		assertEquals(BigDecimal.ZERO, campaignDeviceMonitorService.checkRate(campaignID.toString(), 3));

		// For the given campaign ID, verify a 66% rate for group 1
		assertEquals(new BigDecimal("0.66"), campaignDeviceMonitorService.checkRate(campaignID.toString(), 1));

		// For the given campaign ID, verify a 100% rate for group 2
		assertEquals(new BigDecimal("1.00"), campaignDeviceMonitorService.checkRate(campaignID.toString(), 2));
	}

	@Test
	void save() {

		// Create new entity manually
		CampaignDeviceMonitorEntity newEntity =
			CampaignDeviceMonitorEntity.builder()
				.campaignId(new ObjectId())
				.deviceId(new ObjectId())
				.hardwareId("test-hardware-id")
				.build();

		// Act save new entity
		campaignDeviceMonitorService.save(newEntity);


		// Retrieve all persisted entities
		List<CampaignDeviceMonitorEntity> persistedEntities = testHelper.findAllCampaignDeviceMonitorEntities().list();

		// Verify if only one entity was persisted
		assertEquals(1, persistedEntities.size());

		// Verify the persisted entity for expected values
		CampaignDeviceMonitorEntity persistedEntity = persistedEntities.getFirst();
		assertNotNull(persistedEntity.getId());
		assertEquals(newEntity.getCampaignId(), persistedEntity.getCampaignId());
		assertEquals(newEntity.getDeviceId(), persistedEntity.getDeviceId());
		assertEquals(newEntity.getHardwareId(), persistedEntity.getHardwareId());
		assertNull(persistedEntity.getCommandReplyId());
		assertNull(persistedEntity.getCommandRequestId());

		// Update persisted entity
		persistedEntity.setCommandRequestId(new ObjectId());
		persistedEntity.setCommandReplyId(new ObjectId());

		// Act save updated entity
		campaignDeviceMonitorService.save(persistedEntity);

		// Retrieve all persisted entities
		persistedEntities = testHelper.findAllCampaignDeviceMonitorEntities().list();

		// Verify if it still contains persisted only one entity
		assertEquals(1, persistedEntities.size());

		// Verify the updated entity for expected values
		CampaignDeviceMonitorEntity updatedEntity = persistedEntities.getFirst();
		assertEquals(newEntity.getId(), updatedEntity.getId());
		assertEquals(newEntity.getCampaignId(), updatedEntity.getCampaignId());
		assertEquals(newEntity.getDeviceId(), updatedEntity.getDeviceId());
		assertEquals(newEntity.getHardwareId(), updatedEntity.getHardwareId());
		assertNotNull(updatedEntity.getCommandReplyId());
		assertNotNull(persistedEntity.getCommandRequestId());

	}

	@Test
	void deleteByColumn() {


		// Create six entities with distinct values
		CampaignDeviceMonitorEntity entity1 = testHelper.makeCampaignDeviceMonitorEntity().setGroup(1);
		CampaignDeviceMonitorEntity entity2 = testHelper.makeCampaignDeviceMonitorEntity().setGroup(2);
		CampaignDeviceMonitorEntity entity3 = testHelper.makeCampaignDeviceMonitorEntity().setGroup(3);
		CampaignDeviceMonitorEntity entity4 = testHelper.makeCampaignDeviceMonitorEntity().setGroup(4);
		CampaignDeviceMonitorEntity entity5 = testHelper.makeCampaignDeviceMonitorEntity().setGroup(5);
		CampaignDeviceMonitorEntity entity6 = testHelper.makeCampaignDeviceMonitorEntity().setGroup(6);

		// Persist all entities
		testHelper.persistCampaignDeviceMonitorEntity(entity1);
		testHelper.persistCampaignDeviceMonitorEntity(entity2);
		testHelper.persistCampaignDeviceMonitorEntity(entity3);
		testHelper.persistCampaignDeviceMonitorEntity(entity4);
		testHelper.persistCampaignDeviceMonitorEntity(entity5);
		testHelper.persistCampaignDeviceMonitorEntity(entity6);

		// Act deleting by campaign ID
		campaignDeviceMonitorService.deleteByColumn("campaignId", entity1.getCampaignId());

		// Verify if only the expected entity was removed
		assertEquals(5, testHelper.findAllCampaignDeviceMonitorEntities().stream().count());

		// Act deleting by device ID
		campaignDeviceMonitorService.deleteByColumn("deviceId", entity2.getDeviceId());

		// Verify if only the expected entity was removed
		assertEquals(4, testHelper.findAllCampaignDeviceMonitorEntities().stream().count());

		// Act deleting by group
		campaignDeviceMonitorService.deleteByColumn("group", entity3.getGroup());

		// Verify if only the expected entity was removed
		assertEquals(3, testHelper.findAllCampaignDeviceMonitorEntities().stream().count());

		// Act deleting by Hardware ID
		campaignDeviceMonitorService.deleteByColumn("hardwareId", entity4.getHardwareId());

		// Verify if only the expected entity was removed
		assertEquals(2, testHelper.findAllCampaignDeviceMonitorEntities().stream().count());

		// Act deleting by Command Reply ID
		campaignDeviceMonitorService.deleteByColumn("commandReplyId", entity5.getCommandReplyId());

		// Verify if only the expected entity was removed
		assertEquals(1, testHelper.findAllCampaignDeviceMonitorEntities().stream().count());

		// Act deleting by Command Request ID
		campaignDeviceMonitorService.deleteByColumn("commandRequestId", entity6.getCommandRequestId());

		// Verify if only the expected entity was removed
		assertEquals(0, testHelper.findAllCampaignDeviceMonitorEntities().stream().count());
	}
}
