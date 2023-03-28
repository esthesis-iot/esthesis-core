package esthesis.services.security.impl.service;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.common.AppConstants.Security.Permission;
import esthesis.service.security.entity.GroupEntity;
import esthesis.service.security.entity.PolicyEntity;
import esthesis.service.security.entity.RoleEntity;
import esthesis.service.security.entity.UserEntity;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

  /**
   * Get all permissions for the current user (i.e. the one identified on the JWT).
   *
   * @return
   */
  public List<String> getPermissionsForUser() {
    return getPermissionsForUser(jwt.getName());
  }

  /**
   * Get all permissions for the given user.
   *
   * @param username The username of the user.
   */
  public List<String> getPermissionsForUser(String username) {
//    try {
//      Thread.sleep(2000);
//    } catch (InterruptedException e) {
//      throw new RuntimeException(e);
//    }
    List<String> permissions = new ArrayList<>();

    // Find the user.
    UserEntity userEntity = securityUserService.findByUsername(username);

    // Add all permissions for this user.
    permissions.addAll(userEntity.getPolicies());
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
    String ernPrefix = AppConstants.Security.Ern.ROOT + ":" + AppConstants.Security.Ern.SYSTEM
        + ":" + AppConstants.Security.Ern.SUBSYSTEM;
    String allow = Permission.ALLOW;
    String deny = Permission.DENY;
    if (StringUtils.isBlank(resourceId)) {
      resourceId = "*";
    }
    List<String> permissions = getPermissionsForUser();

    boolean permissionEvaluation =
        !(ListUtils.intersection(permissions, List.of(
            ernPrefix + ":" + category + ":" + resourceId + ":" + operation + ":" + allow,
            ernPrefix + ":" + category + ":" + resourceId + ":*:" + allow,
            permissions, ernPrefix + ":" + category + ":*:*:" + allow,
            permissions, ernPrefix + ":*:*:*:" + allow))).isEmpty() &&
            ListUtils.intersection(permissions, List.of(
                ernPrefix + ":" + category + ":" + resourceId + ":" + operation + ":" + deny,
                ernPrefix + ":" + category + ":" + resourceId + ":*:" + deny,
                permissions, ernPrefix + ":" + category + ":*:*:" + deny,
                permissions, ernPrefix + ":*:*:*:" + deny)).isEmpty();

    log.debug("Permission evaluation for user '{}' on resource '{}:{}:{}' is '{}'.",
        jwt.getName(), category, operation, resourceId, permissionEvaluation);

    return permissionEvaluation;
  }
}
