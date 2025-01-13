package esthesis.services.security.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.audit.resource.AuditSystemResource;
import esthesis.service.security.dto.StatsDTO;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.security.impl.service.SecurityGroupService;
import esthesis.services.security.impl.service.SecurityPermissionsService;
import esthesis.services.security.impl.service.SecurityPolicyService;
import esthesis.services.security.impl.service.SecurityRoleService;
import esthesis.services.security.impl.service.SecurityUserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@RequiredArgsConstructor
public class SecuritySystemResourceImpl implements SecuritySystemResource {

	private final SecurityPermissionsService securityPermissionsService;
	private final SecurityUserService securityUserService;
	private final SecurityGroupService securityGroupService;
	private final SecurityRoleService securityRoleService;
	private final SecurityPolicyService securityPolicyService;

	@Inject
	@RestClient
	AuditSystemResource auditSystemResource;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public boolean isPermitted(Category category, Operation operation, String resourceId,
		ObjectId userId) {
		return securityPermissionsService.isPermitted(category, operation, resourceId,
			securityUserService.findById(userId.toHexString()).getUsername());
	}

	@Override
	public StatsDTO stats() {
		return StatsDTO.builder()
			.users(securityUserService.countAll())
			.groups(securityGroupService.countAll())
			.roles(securityRoleService.countAll())
			.policies(securityPolicyService.countAll())
			.audits(auditSystemResource.countAll())
			.build();
	}
}
