package esthesis.services.security.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.security.entity.GroupEntity;
import esthesis.service.security.entity.PolicyEntity;
import esthesis.service.security.entity.RoleEntity;
import esthesis.service.security.entity.UserEntity;
import esthesis.service.security.resource.SecurityResource;
import esthesis.services.security.impl.service.SecurityGroupService;
import esthesis.services.security.impl.service.SecurityPermissionsService;
import esthesis.services.security.impl.service.SecurityPolicyService;
import esthesis.services.security.impl.service.SecurityRoleService;
import esthesis.services.security.impl.service.SecurityUserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.core.Response;
import java.util.List;

public class SecurityResourceImpl implements SecurityResource {

	@Inject
	SecurityUserService securityUserService;

	@Inject
	SecurityPolicyService securityPolicyService;

	@Inject
	SecurityGroupService securityGroupService;

	@Inject
	SecurityRoleService securityRoleService;

	@Inject
	SecurityPermissionsService securityPermissionsService;

	// ***********************************************************************************************
	// * Users
	// ***********************************************************************************************
	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.USERS, op = Operation.READ, msg = "Search users", log =
		AuditLogType.DATA_IN)
	public Page<UserEntity> findUsers(@BeanParam Pageable pageable) {
		return securityUserService.find(pageable, true);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.USERS, op = Operation.READ, msg = "View user")
	public UserEntity getUser(String userId) {
		return securityUserService.findById(userId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.USERS, op = Operation.DELETE, msg = "Delete user")
	public Response deleteUser(String userId) {
		return securityUserService.deleteById(userId)
			? Response.ok().build()
			: Response.notModified().build();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.USERS, op = Operation.READ, msg = "Save user")
	public UserEntity saveUser(UserEntity userEntity) {
		if (userEntity.getId() == null) {
			return securityUserService.saveNew(userEntity);
		} else {
			return securityUserService.saveUpdate(userEntity);
		}
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getUserPermissions() {
		return securityPermissionsService.getPermissionsForUser();
	}

	// ***********************************************************************************************
	// * Policies
	// ***********************************************************************************************
	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.POLICIES, op = Operation.READ, msg = "Search policies", log =
		AuditLogType.DATA_IN)
	public Page<PolicyEntity> findPolicies(@BeanParam Pageable pageable) {
		return securityPolicyService.find(pageable, true);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.POLICIES, op = Operation.READ, msg = "View policy")
	public PolicyEntity getPolicy(String policyId) {
		return securityPolicyService.findById(policyId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.POLICIES, op = Operation.DELETE, msg = "Delete policy")
	public Response deletePolicy(String policyId) {
		return securityPolicyService.deleteById(policyId)
			? Response.ok().build()
			: Response.notModified().build();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.POLICIES, op = Operation.READ, msg = "Save policy")
	public PolicyEntity savePolicy(PolicyEntity policyEntity) {
		if (policyEntity.getId() == null) {
			return securityPolicyService.saveNew(policyEntity);
		} else {
			return securityPolicyService.saveUpdate(policyEntity);
		}
	}

	// ***********************************************************************************************
	// * Roles
	// ***********************************************************************************************
	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.ROLES, op = Operation.READ, msg = "Search roles", log =
		AuditLogType.DATA_IN)
	public Page<RoleEntity> findRoles(Pageable pageable) {
		return securityRoleService.find(pageable, true);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.ROLES, op = Operation.READ, msg = "View role")
	public RoleEntity getRole(String roleId) {
		return securityRoleService.findById(roleId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.ROLES, op = Operation.DELETE, msg = "Delete role")
	public Response deleteRole(String roleId) {
		return securityRoleService.deleteById(roleId)
			? Response.ok().build()
			: Response.notModified().build();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.ROLES, op = Operation.WRITE, msg = "Save role")
	public RoleEntity saveRole(RoleEntity roleEntity) {
		if (roleEntity.getId() == null) {
			return securityRoleService.saveNew(roleEntity);
		} else {
			return securityRoleService.saveUpdate(roleEntity);
		}
	}

	// ***********************************************************************************************
	// * Groups
	// ***********************************************************************************************
	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.GROUPS, op = Operation.READ, msg = "Search groups", log =
		AuditLogType.DATA_IN)
	public Page<GroupEntity> findGroups(Pageable pageable) {
		return securityGroupService.find(pageable, true);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.GROUPS, op = Operation.READ, msg = "View group")
	public GroupEntity getGroup(String groupId) {
		return securityGroupService.findById(groupId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.GROUPS, op = Operation.DELETE, msg = "Delete group")
	public Response deleteGroup(String groupId) {
		return securityGroupService.deleteById(groupId)
			? Response.ok().build()
			: Response.notModified().build();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.GROUPS, op = Operation.WRITE, msg = "Save group")
	public GroupEntity saveGroup(GroupEntity groupEntity) {
		 if (groupEntity.getId() == null) {
			return securityGroupService.saveNew(groupEntity);
		} else {
			return securityGroupService.saveUpdate(groupEntity);
		}
	}

	// ***********************************************************************************************
	// * Permission check
	// ***********************************************************************************************
	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public boolean isPermitted(Category category, Operation operation, String resourceId) {
		return securityPermissionsService.isPermitted(category, operation, resourceId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public boolean isPermitted(Category category, Operation operation) {
		return securityPermissionsService.isPermitted(category, operation, "*");
	}
}
