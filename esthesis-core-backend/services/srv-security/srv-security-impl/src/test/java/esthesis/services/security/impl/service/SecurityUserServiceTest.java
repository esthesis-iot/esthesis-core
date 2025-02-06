package esthesis.services.security.impl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import esthesis.service.security.entity.UserEntity;
import esthesis.services.security.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SecurityUserServiceTest {

	@Inject
	SecurityUserService securityUserService;

	@Inject
	TestHelper testHelper;

	@ConfigProperty(name = "esthesis.security.admin.username")
	String esthesisAdminUsername;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	void findByUsername() {
		// Perform a save operation for a new user.
		securityUserService.saveNew(testHelper.makeUserEntity("test user"));

		// Assert that the user can be found by its username.
		assertNotNull(securityUserService.findByUsername("test user"));

		// Assert non-existent user cannot be found.
		assertNull(securityUserService.findByUsername("non-existent user"));
	}

	@Test
	void createDefaultAdmin() {
		// Assert that the default admin user cannot be found.
		assertNull(securityUserService.findByUsername(esthesisAdminUsername));

		// Perform the default admin user creation.
		securityUserService.createDefaultAdmin();

		// Assert that the default admin user can be found.
		assertNotNull(securityUserService.findByUsername(esthesisAdminUsername));
	}

	@Test
	void saveNew() {

		// Perform a save operation for a new user.
		String userId =
			securityUserService.saveNew(
					new UserEntity(
						"test user",
						"test@email.eu",
						"firstname",
						"lastname",
						"test description",
						List.of("test group"),
						List.of("test policy")))
				.getId()
				.toHexString();

		// Assert user was saved with correct values.
		UserEntity user = securityUserService.findById(userId);
		assertEquals("test user", user.getUsername());
		assertEquals("test@email.eu", user.getEmail());
		assertEquals("firstname", user.getFirstName());
		assertEquals("lastname", user.getLastName());
		assertEquals("test description", user.getDescription());
		assertEquals(List.of("test group"), user.getGroups());
		assertEquals(List.of("test policy"), user.getPolicies());
	}

	@Test
	void saveUpdate() {
		// Perform a save operation for a new user.
		String userId =
			securityUserService.saveNew(testHelper.makeUserEntity("test user")).getId().toHexString();

		// Find the user in the database.
		UserEntity user = securityUserService.findById(userId);

		// Update the user.
		user.setEmail("updated@email.eu");
		user.setUsername("updated user");
		user.setFirstName("updatedFirstname");
		user.setLastName("updatedLastname");
		user.setDescription("updated description");
		user.setGroups(List.of("updated group"));
		user.setPolicies(List.of("updated policy"));
		securityUserService.saveUpdate(user);

		// Assert user was updated with correct values.
		user = securityUserService.findById(userId);
		assertEquals("updated user", user.getUsername());
		assertEquals("updated@email.eu", user.getEmail());
		assertEquals("updatedFirstname", user.getFirstName());
		assertEquals("updatedLastname", user.getLastName());
		assertEquals("updated description", user.getDescription());
		assertEquals(List.of("updated group"), user.getGroups());
		assertEquals(List.of("updated policy"), user.getPolicies());
	}

	@Test
	void deleteById() {
		// Perform a save operation for a new user.
		String userId =
			securityUserService.saveNew(testHelper.makeUserEntity("test user")).getId().toHexString();

		// Assert user can be found.
		assertNotNull(securityUserService.findById(userId));

		// Delete the user.
		securityUserService.deleteById(userId);

		// Assert user cannot be found.
		assertNull(securityUserService.findById(userId));
	}

	@Test
	void findById() {
		// Perform a save operation for a new user.
		String userId =
			securityUserService.saveNew(testHelper.makeUserEntity("test user")).getId().toHexString();

		// Assert user can be found.
		assertNotNull(securityUserService.findById(userId));
	}

	@Test
	void find() {
		// Assert no users can be found.
		assertTrue(
			securityUserService.find(
					testHelper.makePageable(0, 10))
				.getContent()
				.isEmpty());

		// Perform a save operation for a new user.
		securityUserService.saveNew(testHelper.makeUserEntity("test user"));

		// Assert user can be found.
		assertFalse(
			securityUserService.find(
					testHelper.makePageable(0, 10))
				.getContent()
				.isEmpty());
	}

	@Test
	void find_withPartialMatch() {
		// Assert no users can be found.
		assertTrue(
			securityUserService.find(
					testHelper.makePageable(0, 10))
				.getContent()
				.isEmpty());

		// Perform a save operation for a new user.
		securityUserService.saveNew(testHelper.makeUserEntity("test user"));

		// Assert user can be found.
		assertFalse(
			securityUserService.find(
					testHelper.makePageable(0, 10))
				.getContent()
				.isEmpty());

	}

	@Test
	void countAll() {
		// Assert count of all users is 0.
		assertEquals(0, securityUserService.countAll());

		// Perform a save operation for a new user.
		securityUserService.saveNew(testHelper.makeUserEntity("test user"));

		// Assert count of all users is 1.
		assertEquals(1, securityUserService.countAll());
	}
}
