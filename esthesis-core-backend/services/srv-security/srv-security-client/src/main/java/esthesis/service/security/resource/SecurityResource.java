package esthesis.service.security.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.security.entity.GroupEntity;
import esthesis.service.security.entity.PolicyEntity;
import esthesis.service.security.entity.RoleEntity;
import esthesis.service.security.entity.UserEntity;
import io.quarkus.oidc.token.propagation.common.AccessToken;
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
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the security service.
 */
@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "SecurityResource")
public interface SecurityResource {

	// ***********************************************************************************************
	// * Users
	// ***********************************************************************************************

	/**
	 * Find users.
	 *
	 * @param pageable paging parameters.
	 * @return a page of users.
	 */
	@GET
	@Path("/v1/users/find")
	Page<UserEntity> findUsers(@BeanParam Pageable pageable);

	/**
	 * Find a user by username.
	 *
	 * @param username the username.
	 * @return the user.
	 */
	@GET
	@Path("/v1/users/find/{username}")
	UserEntity findUserByUsername(@PathParam("username") String username);

	/**
	 * Get a user by id.
	 *
	 * @param userId the user id.
	 * @return the user.
	 */
	@GET
	@Path("/v1/users/{userId}")
	UserEntity getUser(@PathParam("userId") String userId);

	/**
	 * Delete a user.
	 *
	 * @param id the user id.
	 * @return the response.
	 */
	@DELETE
	@Path("/v1/users/{userId}")
	Response deleteUser(@PathParam("userId") String id);

	/**
	 * Save a user.
	 *
	 * @param userEntity the user.
	 * @return the saved user.
	 */
	@POST
	@Path("/v1/users")
	@Produces("application/json")
	UserEntity saveUser(@Valid UserEntity userEntity);

	/**
	 * Get the permissions of the current user.
	 *
	 * @return the permissions.
	 */
	@GET
	@Path("/v1/users/user-permissions")
	@Produces("application/json")
	List<String> getUserPermissions();

	// ***********************************************************************************************
	// * Policies
	// ***********************************************************************************************

	/**
	 * Find policies.
	 *
	 * @param pageable paging parameters.
	 * @return a page of policies.
	 */
	@GET
	@Path("/v1/policies/find")
	Page<PolicyEntity> findPolicies(@BeanParam Pageable pageable);

	/**
	 * Get a policy by id.
	 *
	 * @param policyId the policy id.
	 * @return the policy.
	 */
	@GET
	@Path("/v1/policies/{policyId}")
	PolicyEntity getPolicy(@PathParam("policyId") String policyId);

	/**
	 * Delete a policy.
	 *
	 * @param policyId the policy id.
	 * @return the response.
	 */
	@DELETE
	@Path("/v1/policies/{policyId}")
	Response deletePolicy(@PathParam("policyId") String policyId);

	/**
	 * Save a policy.
	 *
	 * @param policyEntity the policy.
	 * @return the saved policy.
	 */
	@POST
	@Path("/v1/policies")
	@Produces("application/json")
	PolicyEntity savePolicy(@Valid PolicyEntity policyEntity);

	// ***********************************************************************************************
	// * Roles
	// ***********************************************************************************************

	/**
	 * Find roles.
	 *
	 * @param pageable paging parameters.
	 * @return a page of roles.
	 */
	@GET
	@Path("/v1/roles/find")
	Page<RoleEntity> findRoles(@BeanParam Pageable pageable);

	/**
	 * Get a role by id.
	 *
	 * @param roleId the role id.
	 * @return the role.
	 */
	@GET
	@Path("/v1/roles/{roleId}")
	RoleEntity getRole(@PathParam("roleId") String roleId);

	/**
	 * Delete a role.
	 *
	 * @param roleId the role id.
	 * @return the response.
	 */
	@DELETE
	@Path("/v1/roles/{roleId}")
	Response deleteRole(@PathParam("roleId") String roleId);

	/**
	 * Save a role.
	 *
	 * @param roleEntity the role.
	 * @return the saved role.
	 */
	@POST
	@Path("/v1/roles")
	@Produces("application/json")
	RoleEntity saveRole(@Valid RoleEntity roleEntity);

	// ***********************************************************************************************
	// * Groups
	// ***********************************************************************************************

	/**
	 * Find groups.
	 *
	 * @param pageable paging parameters.
	 * @return a page of groups.
	 */
	@GET
	@Path("/v1/groups/find")
	Page<GroupEntity> findGroups(@BeanParam Pageable pageable);

	/**
	 * Get a group by id.
	 *
	 * @param groupId the group id.
	 * @return the group.
	 */
	@GET
	@Path("/v1/groups/{groupId}")
	GroupEntity getGroup(@PathParam("groupId") String groupId);

	/**
	 * Delete a group.
	 *
	 * @param groupId the group id.
	 * @return the response.
	 */
	@DELETE
	@Path("/v1/groups/{groupId}")
	Response deleteGroup(@PathParam("groupId") String groupId);

	/**
	 * Save a group.
	 *
	 * @param groupEntity the group.
	 * @return the saved group.
	 */
	@POST
	@Path("/v1/groups")
	@Produces("application/json")
	GroupEntity saveGroup(@Valid GroupEntity groupEntity);

	// ***********************************************************************************************
	// * Permission check
	// ***********************************************************************************************

	/**
	 * Check if the current user is permitted to perform an operation on a resource.
	 *
	 * @param category   the category.
	 * @param operation  the operation.
	 * @param resourceId the resource id.
	 * @return true if the user is permitted.
	 */
	@GET
	@Path("/v1/users/is-permitted/{category}/{operation}/{resourceId}")
	boolean isPermitted(@PathParam("category") AppConstants.Security.Category category,
		@PathParam("operation") AppConstants.Security.Operation operation,
		@PathParam("resourceId") String resourceId);

	/**
	 * Check if the current user is permitted to perform an operation.
	 *
	 * @param category  the category.
	 * @param operation the operation.
	 * @return true if the user is permitted.
	 */
	@GET
	@Path("/v1/users/is-permitted/{category}/{operation}")
	boolean isPermitted(@PathParam("category") AppConstants.Security.Category category,
		@PathParam("operation") AppConstants.Security.Operation operation);
}
