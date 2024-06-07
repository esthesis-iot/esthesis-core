package esthesis.service.security.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.security.entity.GroupEntity;
import esthesis.service.security.entity.PolicyEntity;
import esthesis.service.security.entity.RoleEntity;
import esthesis.service.security.entity.UserEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Settings which can be accessed by unauthenticated users.
 */
@Path("/api")
@RegisterRestClient(configKey = "SecurityResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface SecurityResource {

	// ***********************************************************************************************
	// * Users
	// ***********************************************************************************************
	@GET
	@Path("/v1/users/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<UserEntity> findUsers(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/users/{userId}")
	@RolesAllowed(AppConstants.ROLE_USER)
	UserEntity getUser(@PathParam("userId") String userId);

	@DELETE
	@Path("/v1/users/{userId}")
	@RolesAllowed(AppConstants.ROLE_USER)
	Response deleteUser(@PathParam("userId") String id);

	@POST
	@Path("/v1/users")
	@Produces("application/json")
	@RolesAllowed(AppConstants.ROLE_USER)
	UserEntity saveUser(@Valid UserEntity userEntity);

	@GET
	@Path("/v1/users/user-permissions")
	@Produces("application/json")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<String> getUserPermissions();

	// ***********************************************************************************************
	// * Policies
	// ***********************************************************************************************
	@GET
	@Path("/v1/policies/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<PolicyEntity> findPolicies(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/policies/{policyId}")
	@RolesAllowed(AppConstants.ROLE_USER)
	PolicyEntity getPolicy(@PathParam("policyId") String policyId);

	@DELETE
	@Path("/v1/policies/{policyId}")
	@RolesAllowed(AppConstants.ROLE_USER)
	Response deletePolicy(@PathParam("policyId") String policyId);

	@POST
	@Path("/v1/policies")
	@Produces("application/json")
	@RolesAllowed(AppConstants.ROLE_USER)
	PolicyEntity savePolicy(@Valid PolicyEntity policyEntity);

	// ***********************************************************************************************
	// * Roles
	// ***********************************************************************************************
	@GET
	@Path("/v1/roles/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<RoleEntity> findRoles(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/roles/{roleId}")
	@RolesAllowed(AppConstants.ROLE_USER)
	RoleEntity getRole(@PathParam("roleId") String roleId);

	@DELETE
	@Path("/v1/roles/{roleId}")
	@RolesAllowed(AppConstants.ROLE_USER)
	Response deleteRole(@PathParam("roleId") String roleId);

	@POST
	@Path("/v1/roles")
	@Produces("application/json")
	@RolesAllowed(AppConstants.ROLE_USER)
	RoleEntity saveRole(@Valid RoleEntity roleEntity);

	// ***********************************************************************************************
	// * Groups
	// ***********************************************************************************************
	@GET
	@Path("/v1/groups/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<GroupEntity> findGroups(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/groups/{groupId}")
	@RolesAllowed(AppConstants.ROLE_USER)
	GroupEntity getGroup(@PathParam("groupId") String groupId);

	@DELETE
	@Path("/v1/groups/{groupId}")
	@RolesAllowed(AppConstants.ROLE_USER)
	Response deleteGroup(@PathParam("groupId") String groupId);

	@POST
	@Path("/v1/groups")
	@Produces("application/json")
	@RolesAllowed(AppConstants.ROLE_USER)
	GroupEntity saveGroup(@Valid GroupEntity groupEntity);

	// ***********************************************************************************************
	// * Permission check
	// ***********************************************************************************************
	@GET
	@Path("/v1/users/is-permitted/{category}/{operation}/{resourceId}")
	@RolesAllowed(AppConstants.ROLE_USER)
	boolean isPermitted(@PathParam("category") AppConstants.Security.Category category,
		@PathParam("operation") AppConstants.Security.Operation operation,
		@PathParam("resourceId") String resourceId);

	@GET
	@Path("/v1/users/is-permitted/{category}/{operation}")
	@RolesAllowed(AppConstants.ROLE_USER)
	boolean isPermitted(@PathParam("category") AppConstants.Security.Category category,
		@PathParam("operation") AppConstants.Security.Operation operation);
}
