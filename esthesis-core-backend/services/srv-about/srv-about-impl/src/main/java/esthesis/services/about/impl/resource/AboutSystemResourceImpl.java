package esthesis.services.about.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.about.resource.AboutSystemResource;
import esthesis.services.about.impl.service.AboutService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AboutSystemResourceImpl implements AboutSystemResource {

	private final AboutService aboutService;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public AboutGeneralDTO getGeneralInfo() {
		return aboutService.getGeneralInfo();
	}
}
