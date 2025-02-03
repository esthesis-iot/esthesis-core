package esthesis.service.kubernetes.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.kubernetes.dto.DeploymentInfoDTO;
import esthesis.service.kubernetes.impl.service.KubernetesService;
import esthesis.service.kubernetes.resource.KubernetesResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Implementation of {@link KubernetesResource}.
 */
public class KubernetesResourceImpl implements KubernetesResource {

	@Inject
	KubernetesService kubernetesService;

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public Boolean scheduleDeployment(DeploymentInfoDTO deploymentInfoDTO) {
		return kubernetesService.scheduleDeployment(deploymentInfoDTO);
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
