package esthesis.service.security.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.security.entity.GroupEntity;
import esthesis.service.security.entity.PolicyEntity;
import esthesis.service.security.entity.RoleEntity;
import esthesis.service.security.entity.UserEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
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
	Page<UserEntity> findUsers(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/users/{userId}")
	UserEntity getUser(@PathParam("userId") String userId);

	@DELETE
	@Path("/v1/users/{userId}")
	Response deleteUser(@PathParam("userId") String id);

	@POST
	@Path("/v1/users")
	@Produces("application/json")
	UserEntity saveUser(@Valid UserEntity userEntity);

	@GET
	@Path("/v1/users/user-permissions")
	@Produces("application/json")
	List<String> getUserPermissions();

	// ***********************************************************************************************
	// * Policies
	// ***********************************************************************************************
	@GET
	@Path("/v1/policies/find")
	Page<PolicyEntity> findPolicies(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/policies/{policyId}")
	PolicyEntity getPolicy(@PathParam("policyId") String policyId);

	@DELETE
	@Path("/v1/policies/{policyId}")
	Response deletePolicy(@PathParam("policyId") String policyId);

	@POST
	@Path("/v1/policies")
	@Produces("application/json")
	PolicyEntity savePolicy(@Valid PolicyEntity policyEntity);

	// ***********************************************************************************************
	// * Roles
	// ***********************************************************************************************
	@GET
	@Path("/v1/roles/find")
	Page<RoleEntity> findRoles(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/roles/{roleId}")
	RoleEntity getRole(@PathParam("roleId") String roleId);

	@DELETE
	@Path("/v1/roles/{roleId}")
	Response deleteRole(@PathParam("roleId") String roleId);

	@POST
	@Path("/v1/roles")
	@Produces("application/json")
	RoleEntity saveRole(@Valid RoleEntity roleEntity);

	// ***********************************************************************************************
	// * Groups
	// ***********************************************************************************************
	@GET
	@Path("/v1/groups/find")
	Page<GroupEntity> findGroups(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/groups/{groupId}")
	GroupEntity getGroup(@PathParam("groupId") String groupId);

	@DELETE
	@Path("/v1/groups/{groupId}")
	Response deleteGroup(@PathParam("groupId") String groupId);

	@POST
	@Path("/v1/groups")
	@Produces("application/json")
	GroupEntity saveGroup(@Valid GroupEntity groupEntity);

	// ***********************************************************************************************
	// * Permission check
	// ***********************************************************************************************
	@GET
	@Path("/v1/users/is-permitted/{category}/{operation}/{resourceId}")
	boolean isPermitted(@PathParam("category") AppConstants.Security.Category category,
		@PathParam("operation") AppConstants.Security.Operation operation,
		@PathParam("resourceId") String resourceId);

	@GET
	@Path("/v1/users/is-permitted/{category}/{operation}")
	boolean isPermitted(@PathParam("category") AppConstants.Security.Category category,
		@PathParam("operation") AppConstants.Security.Operation operation);
}
