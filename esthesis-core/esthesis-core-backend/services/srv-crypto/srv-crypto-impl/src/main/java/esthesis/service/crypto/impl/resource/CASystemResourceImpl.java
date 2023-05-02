package esthesis.service.crypto.impl.resource;

import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.resource.CASystemResource;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;

@Authenticated
public class CASystemResourceImpl implements CASystemResource {

	@Inject
	CAService caService;

	@Override
	public String getCACertificate(String caId) {
		return caService.findById(caId).getCertificate();
	}
}
