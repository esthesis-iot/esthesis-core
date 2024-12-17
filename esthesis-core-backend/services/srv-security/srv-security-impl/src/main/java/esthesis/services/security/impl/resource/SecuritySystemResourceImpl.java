package esthesis.services.security.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.security.impl.service.SecurityPermissionsService;
import esthesis.services.security.impl.service.SecurityUserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

@ApplicationScoped
@RequiredArgsConstructor
public class SecuritySystemResourceImpl implements SecuritySystemResource {

	private final SecurityPermissionsService securityPermissionsService;
	private final SecurityUserService securityUserService;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public boolean isPermitted(Category category, Operation operation, String resourceId,
		ObjectId userId) {
		return securityPermissionsService.isPermitted(category, operation, resourceId,
			securityUserService.findById(userId.toHexString()).getUsername());
	}
}
