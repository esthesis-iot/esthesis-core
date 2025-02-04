package esthesis.services.security.impl.service;

import esthesis.service.security.entity.RoleEntity;
import esthesis.services.security.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SecurityRoleServiceTest {

	@Inject
	SecurityRoleService securityRoleService;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	void find() {
		// Assert no roles exist.
		assertTrue(
			securityRoleService.find(testHelper.makePageable(0, 10),
					true)
				.getContent()
				.isEmpty());

		// Perform a save operation for a new role.
		securityRoleService.saveNew(testHelper.makeRoleEntity("test role"));

		// Assert role can be found.
		assertFalse(
			securityRoleService.find(testHelper.makePageable(0, 10),
					true)
				.getContent()
				.isEmpty());
	}

	@Test
	void findById() {
		// Perform a save operation for a new role.
		String roleId =
			securityRoleService.saveNew(testHelper.makeRoleEntity("test role"))
				.getId()
				.toHexString();

		// Assert role can be found.
		assertNotNull(securityRoleService.findById(roleId));
	}

	@Test
	void deleteById() {
		// Perform a save operation for a new role.
		String roleId =
			securityRoleService.saveNew(testHelper.makeRoleEntity("test role"))
				.getId()
				.toHexString();

		// Assert role can be found.
		assertNotNull(securityRoleService.findById(roleId));

		// Perform a delete operation for the given role ID.
		securityRoleService.deleteById(roleId);

		// Assert role cannot be found.
		assertNull(securityRoleService.findById(roleId));
	}

	@Test
	void saveNew() {
		// Perform a save operation for a new role.
		securityRoleService.saveNew(
			new RoleEntity(
				"test role",
				"test description",
				List.of("test policy")));

		// Assert role was saved with correct values.
		RoleEntity role = securityRoleService.find(testHelper.makePageable(0, 10), true).getContent().get(0);
		assertEquals("test role", role.getName());
		assertEquals("test description", role.getDescription());
		assertEquals(List.of("test policy"), role.getPolicies());
	}

	@Test
	void saveUpdate() {
		// Perform a save operation for a new role.
		String roleId =
			securityRoleService.saveNew(testHelper.makeRoleEntity("test role"))
				.getId()
				.toHexString();

		// Find the role in the database.
		RoleEntity role = securityRoleService.findById(roleId);

		// Perform an update operation for the role.
		role.setName("new role");
		role.setDescription("new description");
		role.setPolicies(List.of("new policy"));
		securityRoleService.saveUpdate(role);

		// Assert role was updated with correct values.
		role = securityRoleService.findById(roleId);
		assertEquals("new role", role.getName());
		assertEquals("new description", role.getDescription());
		assertEquals(List.of("new policy"), role.getPolicies());
	}

	@Test
	void countAll() {
		// Assert count of all roles is 0.
		assertEquals(0, securityRoleService.countAll());

		// Perform a save operation for a new role.
		securityRoleService.saveNew(testHelper.makeRoleEntity("test role"));

		// Assert count of all roles is 1.
		assertEquals(1, securityRoleService.countAll());
	}
}
