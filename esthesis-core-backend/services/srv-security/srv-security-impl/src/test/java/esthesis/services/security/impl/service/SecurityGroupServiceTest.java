package esthesis.services.security.impl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import esthesis.service.security.entity.GroupEntity;
import esthesis.services.security.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SecurityGroupServiceTest {

	@Inject
	SecurityGroupService securityGroupService;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	void find() {
		// Assert no groups exist.
		assertTrue(
			securityGroupService.find(testHelper.makePageable(1, 10))
				.getContent()
				.isEmpty());

		// Perform a save operation for a new group.
		securityGroupService.saveNew(testHelper.makeGroupEntity("test group"));

		// Assert group can be found.
		assertFalse(
			securityGroupService.find(
					testHelper.makePageable(0, 10))
				.getContent()
				.isEmpty());

		assertFalse(
			securityGroupService.find(
					testHelper.makePageable(0, 10))
				.getContent()
				.isEmpty());
	}

	@Test
	void findById() {
		// Perform a save operation for a new group.
		String groupId =
			securityGroupService.saveNew(testHelper.makeGroupEntity("test group")).getId().toHexString();

		// Assert group can be found.
		assertNotNull(securityGroupService.findById(groupId));

	}

	@Test
	void deleteById() {
		// Perform a save operation for a new group.
		String groupId =
			securityGroupService.saveNew(testHelper.makeGroupEntity("test group")).getId().toHexString();

		// Assert group can be found.
		assertNotNull(securityGroupService.findById(groupId));

		// Perform a delete operation for the given group ID.
		securityGroupService.deleteById(groupId);

		// Assert group cannot be found.
		assertNull(securityGroupService.findById(groupId));
	}

	@Test
	void saveNew() {
		// Perform a save operation for a new group.
		String groupId =
			securityGroupService.saveNew(
					new GroupEntity(
						"test group",
						"test description",
						List.of("test role"),
						List.of("test policy")))
				.getId()
				.toHexString();

		// Assert group was saved with correct values.
		GroupEntity group = securityGroupService.findById(groupId);
		assertEquals("test group", group.getName());
		assertEquals("test description", group.getDescription());
		assertEquals(List.of("test role"), group.getRoles());
		assertEquals(List.of("test policy"), group.getPolicies());
	}

	@Test
	void saveUpdate() {
		// Perform a save operation for a new group.
		String groupId =
			securityGroupService.saveNew(testHelper.makeGroupEntity("test group")).getId().toHexString();

		// Find the group by ID.
		GroupEntity group = securityGroupService.findById(groupId);

		// Perform an update operation for the given group.
		group.setName("updated name");
		group.setDescription("updated description");
		group.setRoles(List.of("updated role"));
		group.setPolicies(List.of("updated policy"));
		securityGroupService.saveUpdate(group);

		// Assert group was updated with correct values.
		group = securityGroupService.findById(groupId);
		assertEquals("updated name", group.getName());
		assertEquals("updated description", group.getDescription());
		assertEquals(List.of("updated role"), group.getRoles());
		assertEquals(List.of("updated policy"), group.getPolicies());

	}

	@Test
	void countAll() {
		// Assert count of all groups is 0.
		assertEquals(0, securityGroupService.countAll());

		// Perform a save operation for a new group.
		securityGroupService.saveNew(testHelper.makeGroupEntity("test group"));

		// Assert count of all groups is 1.
		assertEquals(1, securityGroupService.countAll());
	}
}
