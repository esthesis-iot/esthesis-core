package esthesis.service.application.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.application.impl.service.ApplicationService;
import esthesis.service.application.resource.ApplicationSystemResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

public class ApplicationSystemResourceImpl implements ApplicationSystemResource {

	@Inject
	ApplicationService applicationService;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public boolean isTokenValid(String token) {
		return applicationService.isTokenValid(token);
	}
}
