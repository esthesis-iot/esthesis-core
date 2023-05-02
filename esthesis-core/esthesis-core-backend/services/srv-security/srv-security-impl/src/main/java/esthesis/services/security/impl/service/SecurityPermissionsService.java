package esthesis.services.security.impl.service;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.common.AppConstants.Security.Permission;
import esthesis.service.security.entity.GroupEntity;
import esthesis.service.security.entity.PolicyEntity;
import esthesis.service.security.entity.RoleEntity;
import esthesis.service.security.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@ApplicationScoped
public class SecurityPermissionsService {

	@Inject
	JsonWebToken jwt;

	@Inject
	SecurityUserService securityUserService;

	@Inject
	SecurityRoleService securityRoleService;

	@Inject
	SecurityGroupService securityGroupService;

	@Inject
	SecurityPolicyService securityPolicyService;

	private String ernPrefix = AppConstants.Security.Ern.ROOT + ":" + AppConstants.Security.Ern.SYSTEM
		+ ":" + AppConstants.Security.Ern.SUBSYSTEM;

	/**
	 * Check if the current user is permitted to perform the given operation on the given resource.
	 *
	 * @param category
	 * @param operation
	 * @param resourceId
	 * @return
	 */
	public boolean isPermitted(AppConstants.Security.Category category, Operation operation,
		String resourceId) {
		if (StringUtils.isBlank(resourceId)) {
			resourceId = "*";
		}
		List<String> permissions = getPermissionsForUser();
		boolean permissionAllowed = isPermission(permissions, Permission.ALLOW, category, operation,
			resourceId);
		boolean permissionDenied = isPermission(permissions, Permission.DENY, category, operation,
			resourceId);
		boolean permissionEvaluation = permissionAllowed && !permissionDenied;
		log.debug("Permission evaluation for user '{}' on resource '{}:{}:{}' is '{}' [allowed='{}', "
				+ "denied='{}'].",
			jwt.getName(), category, operation, resourceId, permissionEvaluation, permissionAllowed,
			permissionDenied);

		return permissionEvaluation;
	}

	/**
	 * Get all permissions for the current user (i.e. the one identified on the JWT).
	 *
	 * @return
	 */
	public List<String> getPermissionsForUser() {
		return getPermissionsForUser(jwt.getName());
	}

	private boolean isPermission(List<String> permissions,
		AppConstants.Security.Permission permission, AppConstants.Security.Category category,
		Operation operation, String resourceId) {

		String permissionText = permission.name().toLowerCase();
		String categoryText = category.name().toLowerCase();
		String operationText = operation.name().toLowerCase();
		return permissions.contains(
			ernPrefix + ":" + categoryText + ":" + resourceId + ":" + operationText + ":"
				+ permissionText) ||
			permissions.contains(
				ernPrefix + ":" + categoryText + ":" + resourceId + ":*:" + permissionText)
			||
			permissions.contains(
				ernPrefix + ":" + categoryText + ":*:" + operationText + ":" + permissionText)
			||
			permissions.contains(ernPrefix + ":" + categoryText + ":*:*:" + permissionText) ||
			permissions.contains(ernPrefix + ":*:*:*:" + permissionText);
	}

	/**
	 * Get all permissions for the given user.
	 *
	 * @param username The username of the user.
	 */
	public List<String> getPermissionsForUser(String username) {
		log.debug("Getting permissions for user '{}'.", username);

		// Find the user.
		UserEntity userEntity = securityUserService.findByUsername(username);

		// Add all permissions for this user.
		List<String> permissions = new ArrayList<>(ListUtils.emptyIfNull(userEntity.getPolicies()));
		userEntity.getGroups().forEach(group -> {
			// Iterate each group to find the roles assigned to this group.
			GroupEntity groupEntity = securityGroupService.findById(group);
			if (CollectionUtils.isNotEmpty(groupEntity.getPolicies())) {
				permissions.addAll(groupEntity.getPolicies());
			}
			List<String> roles = groupEntity.getRoles();
			roles.forEach(role -> {
				RoleEntity roleEntity = securityRoleService.findById(role);
				// For each role, collect the policies.
				List<String> policies = roleEntity.getPolicies();
				policies.forEach(policy -> {
					PolicyEntity policyEntity = securityPolicyService.findById(policy);
					permissions.add(policyEntity.getRule());
				});
			});
		});

		return permissions;
	}
}
