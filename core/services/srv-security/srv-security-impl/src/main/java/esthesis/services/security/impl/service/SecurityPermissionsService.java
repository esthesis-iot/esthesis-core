package esthesis.services.security.impl.service;

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

@Slf4j
@ApplicationScoped
public class SecurityPermissionsService {

  @Inject
  SecurityUserService securityUserService;

  @Inject
  SecurityRoleService securityRoleService;

  @Inject
  SecurityGroupService securityGroupService;

  @Inject
  SecurityPolicyService securityPolicyService;

  public List<String> getPermissionsForUser(String username) {
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
}
