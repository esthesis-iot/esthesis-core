package esthesis.service.kubernetes.impl.resource;

import esthesis.service.kubernetes.dto.PodInfoDTO;
import esthesis.service.kubernetes.impl.service.KubernetesService;
import esthesis.service.kubernetes.resource.KubernetesResource;
import jakarta.inject.Inject;
import java.util.List;

public class KubernetesResourceImpl implements KubernetesResource {

	@Inject
	KubernetesService kubernetesService;

	@Override
	public Boolean schedulePod(PodInfoDTO podInfoDTO) {
		return kubernetesService.schedulePod(podInfoDTO);
	}

	@Override
	public List<String> getNamespaces() {
		return kubernetesService.getNamespaces();
	}
}
