package esthesis.services.security.impl.resource;

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
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.core.Response;

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
  @Audited(cat = Category.USERS, op = Operation.READ, msg = "Search users", log =
      AuditLogType.DATA_IN)
  public Page<UserEntity> findUsers(@BeanParam Pageable pageable) {
    return securityUserService.find(pageable, true);
  }

  @Override
  @Audited(cat = Category.USERS, op = Operation.READ, msg = "View user")
  public UserEntity getUser(String userId) {
    return securityUserService.findById(userId);
  }

  @Override
  @Audited(cat = Category.USERS, op = Operation.DELETE, msg = "Delete user")
  public Response deleteUser(String userId) {
    return securityUserService.deleteById(userId)
        ? Response.ok().build()
        : Response.notModified().build();
  }

  @Override
  @Audited(cat = Category.USERS, op = Operation.READ, msg = "Save user")
  public UserEntity saveUser(UserEntity userEntity) {
    return securityUserService.save(userEntity);
  }

  @Override
  public List<String> getUserPermissions() {
    return securityPermissionsService.getPermissionsForUser();
  }

  // ***********************************************************************************************
  // * Policies
  // ***********************************************************************************************
  @Override
  @Audited(cat = Category.POLICIES, op = Operation.READ, msg = "Search policies", log =
      AuditLogType.DATA_IN)
  public Page<PolicyEntity> findPolicies(@BeanParam Pageable pageable) {
    return securityPolicyService.find(pageable, true);
  }

  @Override
  @Audited(cat = Category.POLICIES, op = Operation.READ, msg = "View policy")
  public PolicyEntity getPolicy(String policyId) {
    return securityPolicyService.findById(policyId);
  }

  @Override
  @Audited(cat = Category.POLICIES, op = Operation.DELETE, msg = "Delete policy")
  public Response deletePolicy(String policyId) {
    return securityPolicyService.deleteById(policyId)
        ? Response.ok().build()
        : Response.notModified().build();
  }

  @Override
  @Audited(cat = Category.POLICIES, op = Operation.READ, msg = "Save policy")
  public PolicyEntity savePolicy(PolicyEntity policyEntity) {
    return securityPolicyService.save(policyEntity);
  }

  // ***********************************************************************************************
  // * Roles
  // ***********************************************************************************************
  @Override
  @Audited(cat = Category.ROLES, op = Operation.READ, msg = "Search roles", log =
      AuditLogType.DATA_IN)
  public Page<RoleEntity> findRoles(Pageable pageable) {
    return securityRoleService.find(pageable, true);
  }

  @Override
  @Audited(cat = Category.ROLES, op = Operation.READ, msg = "View role")
  public RoleEntity getRole(String roleId) {
    return securityRoleService.findById(roleId);
  }

  @Override
  @Audited(cat = Category.ROLES, op = Operation.DELETE, msg = "Delete role")
  public Response deleteRole(String roleId) {
    return securityRoleService.deleteById(roleId)
        ? Response.ok().build()
        : Response.notModified().build();
  }

  @Override
  @Audited(cat = Category.ROLES, op = Operation.WRITE, msg = "Save role")
  public RoleEntity saveRole(RoleEntity roleEntity) {
    return securityRoleService.save(roleEntity);
  }

  // ***********************************************************************************************
  // * Groups
  // ***********************************************************************************************
  @Override
  @Audited(cat = Category.GROUPS, op = Operation.READ, msg = "Search groups", log =
      AuditLogType.DATA_IN)
  public Page<GroupEntity> findGroups(Pageable pageable) {
    return securityGroupService.find(pageable, true);
  }

  @Override
  @Audited(cat = Category.GROUPS, op = Operation.READ, msg = "View group")
  public GroupEntity getGroup(String groupId) {
    return securityGroupService.findById(groupId);
  }

  @Override
  @Audited(cat = Category.GROUPS, op = Operation.DELETE, msg = "Delete group")
  public Response deleteGroup(String groupId) {
    return securityGroupService.deleteById(groupId)
        ? Response.ok().build()
        : Response.notModified().build();
  }

  @Override
  @Audited(cat = Category.GROUPS, op = Operation.WRITE, msg = "Save group")
  public GroupEntity saveGroup(GroupEntity groupEntity) {
    return securityGroupService.save(groupEntity);
  }

  // ***********************************************************************************************
  // * Permission check
  // ***********************************************************************************************
  @Override
  public boolean isPermitted(Category category, Operation operation, String resourceId) {
    return securityPermissionsService.isPermitted(category, operation, resourceId);
  }

  @Override
  public boolean isPermitted(Category category, Operation operation) {
    return securityPermissionsService.isPermitted(category, operation, "*");
  }
}
