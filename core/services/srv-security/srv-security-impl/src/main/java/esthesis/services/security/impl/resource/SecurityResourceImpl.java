package esthesis.services.security.impl.resource;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
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
import esthesis.services.security.impl.service.SecurityPolicyService;
import esthesis.services.security.impl.service.SecurityRoleService;
import esthesis.services.security.impl.service.SecurityUserService;
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

  // ***********************************************************************************************
  // * Users
  // ***********************************************************************************************
  @Override
  @Audited(cat = Category.USERS, op = Operation.RETRIEVE, msg = "Search users", log =
      AuditLogType.DATA_IN)
  public Page<UserEntity> findUsers(@BeanParam Pageable pageable) {
    return securityUserService.find(pageable, true);
  }

  @Override
  @Audited(cat = Category.USERS, op = Operation.RETRIEVE, msg = "View user")
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
  @Audited(cat = Category.USERS, op = Operation.RETRIEVE, msg = "Save user")
  public UserEntity saveUser(UserEntity userEntity) {
    return securityUserService.save(userEntity);
  }

  // ***********************************************************************************************
  // * Policies
  // ***********************************************************************************************
  @Override
  @Audited(cat = Category.POLICIES, op = Operation.RETRIEVE, msg = "Search policies", log =
      AuditLogType.DATA_IN)
  public Page<PolicyEntity> findPolicies(@BeanParam Pageable pageable) {
    return securityPolicyService.find(pageable, true);
  }

  @Override
  @Audited(cat = Category.POLICIES, op = Operation.RETRIEVE, msg = "View policy")
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
  @Audited(cat = Category.POLICIES, op = Operation.RETRIEVE, msg = "Save policy")
  public PolicyEntity savePolicy(PolicyEntity policyEntity) {
    return securityPolicyService.save(policyEntity);
  }

  // ***********************************************************************************************
  // * Roles
  // ***********************************************************************************************
  @Override
  @Audited(cat = Category.ROLES, op = Operation.RETRIEVE, msg = "Search roles", log =
      AuditLogType.DATA_IN)
  public Page<RoleEntity> findRoles(Pageable pageable) {
    return securityRoleService.find(pageable, true);
  }

  @Override
  @Audited(cat = Category.ROLES, op = Operation.RETRIEVE, msg = "View role")
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
  @Audited(cat = Category.ROLES, op = Operation.UPDATE, msg = "Save role")
  public RoleEntity saveRole(RoleEntity roleEntity) {
    return securityRoleService.save(roleEntity);
  }

  // ***********************************************************************************************
  // * Groups
  // ***********************************************************************************************
  @Override
  @Audited(cat = Category.GROUPS, op = Operation.RETRIEVE, msg = "Search groups", log =
      AuditLogType.DATA_IN)
  public Page<GroupEntity> findGroups(Pageable pageable) {
    return securityGroupService.find(pageable, true);
  }

  @Override
  @Audited(cat = Category.GROUPS, op = Operation.RETRIEVE, msg = "View group")
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
  @Audited(cat = Category.GROUPS, op = Operation.UPDATE, msg = "Save group")
  public GroupEntity saveGroup(GroupEntity groupEntity) {
    return securityGroupService.save(groupEntity);
  }

}
