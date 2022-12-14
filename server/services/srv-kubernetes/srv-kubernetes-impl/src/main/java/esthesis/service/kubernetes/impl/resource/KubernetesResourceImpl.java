package esthesis.service.kubernetes.impl.resource;

import esthesis.service.kubernetes.dto.PodInfoDTO;
import esthesis.service.kubernetes.impl.service.KubernetesService;
import esthesis.service.kubernetes.resource.KubernetesResource;
import java.util.List;
import javax.inject.Inject;

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
