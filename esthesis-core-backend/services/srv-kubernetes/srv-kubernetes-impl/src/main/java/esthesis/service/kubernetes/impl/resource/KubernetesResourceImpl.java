package esthesis.service.kubernetes.impl.resource;

import esthesis.common.AppConstants;
import esthesis.service.kubernetes.dto.PodInfoDTO;
import esthesis.service.kubernetes.impl.service.KubernetesService;
import esthesis.service.kubernetes.resource.KubernetesResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import java.util.List;

public class KubernetesResourceImpl implements KubernetesResource {

	@Inject
	KubernetesService kubernetesService;

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public Boolean schedulePod(PodInfoDTO podInfoDTO) {
		return kubernetesService.schedulePod(podInfoDTO);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getNamespaces() {
		return kubernetesService.getNamespaces();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public Boolean isDeploymentNameAvailable(String name, String namespace) {
		return kubernetesService.isDeploymentNameAvailable(name, namespace);
	}
}
