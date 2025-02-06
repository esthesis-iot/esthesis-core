package esthesis.services.security.impl.service;

import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.security.entity.GroupEntity;
import esthesis.service.security.entity.PolicyEntity;
import esthesis.service.security.entity.UserEntity;
import esthesis.services.security.impl.TestHelper;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class SecurityPermissionsServiceTest {

	@Inject
	SecurityPermissionsService securityPermissionsService;

	@Inject
	SecurityGroupService securityGroupService;

	@Inject
	SecurityPolicyService securityPolicyService;

	@Inject
	SecurityUserService securityUserService;

	@InjectMock
	SecurityIdentity securityIdentity;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		// Mock the security identity for getting the current user.
		Mockito.when(securityIdentity.getPrincipal()).thenReturn(testHelper.makePrincipal("principal-user"));
	}

	@Test
	void isPermittedCurrentUser() {

		// Perform save operation for a new group without any policies.
		String groupId =
			securityGroupService.saveNew(
					testHelper.makeGroupEntity(
						"test group",
						List.of()))
				.getId()
				.toHexString();

		// Perform save operation for a new user belonging to the group created without any policies.
		String userId =
			securityUserService.saveNew(
					testHelper.makeUserEntity(
						"principal-user",
						List.of(),
						List.of(groupId)))
				.getId()
				.toHexString();

		// Assert permission is not granted for the user.
		assertFalse(
			securityPermissionsService.isPermitted(
				Category.DEVICE,
				Operation.DELETE,
				"*"));


		// Perform save operations for a new policy that allows deleting devices.
		PolicyEntity policy =
			securityPolicyService.saveNew(
				testHelper.makePolicyEntity(
					"allow managing device",
					"ern:esthesis:core:device:*:*:allow"));

		// Update the user with the policy that allows deleting devices.
		UserEntity user = securityUserService.findById(userId);
		user.setPolicies(List.of(policy.getRule()));
		securityUserService.saveUpdate(user);


		// Assert permission is granted for the user to delete devices.
		assertTrue(
			securityPermissionsService.isPermitted(
				Category.DEVICE,
				Operation.DELETE,
				"*"));

		// Assert if no resource ID is provided, permission is still granted for the user to delete devices.
		assertTrue(
			securityPermissionsService.isPermitted(
				Category.DEVICE,
				Operation.DELETE,
				null));

		// Assert a different category permission is not granted for the user.
		assertFalse(
			securityPermissionsService.isPermitted(
				Category.CAMPAIGN,
				Operation.DELETE,
				"*"));

	}

	@Test
	void isPermitted() {
		// Perform save operation for a new group without any policies.
		String groupId =
			securityGroupService.saveNew(
					testHelper.makeGroupEntity(
						"test group",
						List.of()))
				.getId()
				.toHexString();

		// Perform save operation for a new user belonging to the group created without any policies.
		String userId =
			securityUserService.saveNew(
					testHelper.makeUserEntity(
						"test-user",
						List.of(),
						List.of(groupId)))
				.getId()
				.toHexString();

		// Assert permission is not granted for the user.
		assertFalse(
			securityPermissionsService.isPermitted(
				Category.DEVICE,
				Operation.DELETE,
				"*",
				"test-user"));


		// Perform save operations for a new policy that allows deleting devices.
		PolicyEntity policy =
			securityPolicyService.saveNew(
				testHelper.makePolicyEntity(
					"allow managing device",
					"ern:esthesis:core:device:*:*:allow"));

		// Update the user with the policy that allows deleting devices.
		UserEntity user = securityUserService.findById(userId);
		user.setPolicies(List.of(policy.getRule()));
		securityUserService.saveUpdate(user);


		// Assert permission is granted for the user to delete devices.
		assertTrue(
			securityPermissionsService.isPermitted(
				Category.DEVICE,
				Operation.DELETE,
				"*",
				"test-user"));

		// Assert if no resource ID is provided, permission is still granted for the user to delete devices.
		assertTrue(
			securityPermissionsService.isPermitted(
				Category.DEVICE,
				Operation.DELETE,
				null,
				"test-user"));

		// Assert a different category permission is not granted for the user.
		assertFalse(
			securityPermissionsService.isPermitted(
				Category.CAMPAIGN,
				Operation.DELETE,
				"*",
				"test-user"));
	}

	@Test
	void getPermissionsForCurrentUser() {
		// Perform save operation for a new group without any policies.
		String groupId =
			securityGroupService.saveNew(
					testHelper.makeGroupEntity(
						"test group",
						List.of()))
				.getId()
				.toHexString();

		// Perform save operation for a new user belonging to the group created without any policies.
		securityUserService.saveNew(
			testHelper.makeUserEntity(
				"principal-user",
				List.of(),
				List.of(groupId)));

		// Assert user has no permissions.
		assertTrue(securityPermissionsService.getPermissionsForUser().isEmpty());

		// Add permissions to group that the user belongs to.
		GroupEntity group = securityGroupService.findById(groupId);
		group.setPolicies(List.of("test policy"));
		securityGroupService.saveUpdate(group);

		// Assert user has permissions.
		assertFalse(securityPermissionsService.getPermissionsForUser().isEmpty());

	}

	@Test
	void GetPermissionsForUser() {
		// Perform save operation for a new group without any policies.
		String groupId =
			securityGroupService.saveNew(
					testHelper.makeGroupEntity(
						"test group",
						List.of()))
				.getId()
				.toHexString();

		// Perform save operation for a new user belonging to the group created without any policies.
		securityUserService.saveNew(
			testHelper.makeUserEntity(
				"test-user",
				List.of(),
				List.of(groupId)));

		// Assert user has no permissions.
		assertTrue(securityPermissionsService.getPermissionsForUser("test-user").isEmpty());

		// Add permissions to group that the user belongs to.
		GroupEntity group = securityGroupService.findById(groupId);
		group.setPolicies(List.of("test policy"));
		securityGroupService.saveUpdate(group);

		// Assert user has permissions.
		assertFalse(securityPermissionsService.getPermissionsForUser("test-user").isEmpty());
	}
}
