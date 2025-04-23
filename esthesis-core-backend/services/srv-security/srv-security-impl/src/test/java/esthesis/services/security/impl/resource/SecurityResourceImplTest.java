package esthesis.services.security.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.security.entity.GroupEntity;
import esthesis.service.security.entity.PolicyEntity;
import esthesis.service.security.entity.RoleEntity;
import esthesis.service.security.entity.UserEntity;
import esthesis.service.security.resource.SecurityResource;
import esthesis.services.security.impl.TestHelper;
import esthesis.services.security.impl.service.SecurityGroupService;
import esthesis.services.security.impl.service.SecurityPolicyService;
import esthesis.services.security.impl.service.SecurityRoleService;
import esthesis.services.security.impl.service.SecurityUserService;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(SecurityResource.class)
class SecurityResourceImplTest {

	@Inject
	SecurityUserService securityUserService;

	@Inject
	SecurityPolicyService securityPolicyService;

	@Inject
	SecurityGroupService securityGroupService;

	@Inject
	SecurityRoleService securityRoleService;

	@Inject
	TestHelper testHelper;


	@InjectMock
	SecurityIdentity securityIdentity;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		// Mock the security identity for getting the current user.
		Mockito.when(securityIdentity.getPrincipal()).thenReturn(testHelper.makePrincipal("test-user"));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findUsers() {

		securityUserService.saveNew(testHelper.makeUserEntity("test-user"));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/users/find")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findUserByUsername() {
		securityUserService.saveNew(testHelper.makeUserEntity("test-user"));

		given()
			.accept(ContentType.JSON)
			.pathParam("username", "test-user")
			.when().get("/v1/users/find/{username}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getUser() {
		UserEntity user = securityUserService.saveNew(testHelper.makeUserEntity("test-user"));

		given()
			.accept(ContentType.JSON)
			.pathParam("userId", user.getId().toHexString())
			.when().get("/v1/users/{userId}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void deleteUser() {
		UserEntity user = securityUserService.saveNew(testHelper.makeUserEntity("test-user"));

		given()
			.accept(ContentType.JSON)
			.pathParam("userId", user.getId().toHexString())
			.when().delete("/v1/users/{userId}")
			.then()
			.log().all()
			.statusCode(200);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void saveUser() {
		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(testHelper.makeUserEntity("test-user"))
			.when().post("/v1/users")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getUserPermissions() {
		GroupEntity group = securityGroupService.saveNew(testHelper.makeGroupEntity("test-group"));
		securityUserService.saveNew(testHelper.makeUserEntity("test-user", List.of(), List.of(group.getId().toHexString())));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/users/user-permissions")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findPolicies() {
		securityPolicyService.saveNew(testHelper.makePolicyEntity("test-policy"));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/policies/find")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getPolicy() {
		PolicyEntity policy = securityPolicyService.saveNew(testHelper.makePolicyEntity("test-policy"));

		given()
			.accept(ContentType.JSON)
			.pathParam("policyId", policy.getId().toHexString())
			.when().get("/v1/policies/{policyId}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void deletePolicy() {
		PolicyEntity policy = securityPolicyService.saveNew(testHelper.makePolicyEntity("test-policy"));

		given()
			.accept(ContentType.JSON)
			.pathParam("policyId", policy.getId().toHexString())
			.when().delete("/v1/policies/{policyId}")
			.then()
			.log().all()
			.statusCode(200);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void savePolicy() {
		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(testHelper.makePolicyEntity("test-policy"))
			.when().post("/v1/policies")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findRoles() {
		securityRoleService.saveNew(testHelper.makeRoleEntity("test-role"));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/roles/find")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getRole() {
		RoleEntity role = securityRoleService.saveNew(testHelper.makeRoleEntity("test-role"));

		given()
			.accept(ContentType.JSON)
			.pathParam("roleId", role.getId().toHexString())
			.when().get("/v1/roles/{roleId}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void deleteRole() {
		RoleEntity role = securityRoleService.saveNew(testHelper.makeRoleEntity("test-role"));

		given()
			.accept(ContentType.JSON)
			.pathParam("roleId", role.getId().toHexString())
			.when().delete("/v1/roles/{roleId}")
			.then()
			.log().all()
			.statusCode(200);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void saveRole() {
		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(testHelper.makeRoleEntity("test-role"))
			.when().post("/v1/roles")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findGroups() {
		securityGroupService.saveNew(testHelper.makeGroupEntity("test-group"));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/groups/find")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getGroup() {
		GroupEntity group = securityGroupService.saveNew(testHelper.makeGroupEntity("test-group"));

		given()
			.accept(ContentType.JSON)
			.pathParam("groupId", group.getId().toHexString())
			.when().get("/v1/groups/{groupId}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void deleteGroup() {
		GroupEntity group = securityGroupService.saveNew(testHelper.makeGroupEntity("test-group"));

		given()
			.accept(ContentType.JSON)
			.pathParam("groupId", group.getId().toHexString())
			.when().delete("/v1/groups/{groupId}")
			.then()
			.log().all()
			.statusCode(200);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void saveGroup() {
		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(testHelper.makeGroupEntity("test-group"))
			.when().post("/v1/groups")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void isPermitted() {
		GroupEntity group = securityGroupService.saveNew(testHelper.makeGroupEntity("test-group"));
		securityUserService.saveNew(testHelper.makeUserEntity(
			"test-user",
			List.of(),
			List.of(group.getId().toHexString())));

		given()
			.accept(ContentType.JSON)
			.pathParam("category", AppConstants.Security.Category.GROUPS)
			.pathParam("operation", AppConstants.Security.Operation.READ)
			.pathParam("resourceId", "test-group")
			.when().get("/v1/users/is-permitted/{category}/{operation}/{resourceId}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void testIsPermitted() {
		GroupEntity group = securityGroupService.saveNew(testHelper.makeGroupEntity("test-group"));
		securityUserService.saveNew(testHelper.makeUserEntity("test-user", List.of(), List.of(group.getId().toHexString())));

		given()
			.accept(ContentType.JSON)
			.pathParam("category", AppConstants.Security.Category.GROUPS)
			.pathParam("operation", AppConstants.Security.Operation.READ)
			.when().get("/v1/users/is-permitted/{category}/{operation}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}
}
