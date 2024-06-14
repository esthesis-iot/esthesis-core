package esthesis.services.about.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.about.resource.AboutResource;
import esthesis.service.audit.ccc.Audited;
import esthesis.services.about.impl.service.AboutService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

public class AboutResourceImpl implements AboutResource {

	@Inject
	AboutService aboutService;

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.ABOUT, op = Operation.READ, msg = "About/General page")
	public AboutGeneralDTO getGeneralInfo() {
		return aboutService.getGeneralInfo();
	}
}
