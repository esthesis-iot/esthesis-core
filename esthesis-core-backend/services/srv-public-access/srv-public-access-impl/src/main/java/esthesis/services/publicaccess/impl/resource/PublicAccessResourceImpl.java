package esthesis.services.publicaccess.impl.resource;

import esthesis.common.AppConstants;
import esthesis.service.publicaccess.dto.OidcConfigDTO;
import esthesis.service.publicaccess.resource.PublicAccessResource;
import esthesis.services.publicaccess.impl.service.PublicAccessService;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class PublicAccessResourceImpl implements PublicAccessResource {

	@Inject
	PublicAccessService publicAccessService;

	@Override
	public OidcConfigDTO getOidcConfig() {
		return publicAccessService.getOidcConfig();
	}
}
