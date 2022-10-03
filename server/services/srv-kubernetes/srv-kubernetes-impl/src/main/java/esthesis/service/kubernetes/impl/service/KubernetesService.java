package esthesis.service.kubernetes.impl.service;

import esthesis.service.kubernetes.dto.PodInfo;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class KubernetesService {

  @Inject
  private KubernetesClient kc;

  private List<EnvVar> getEnvVar(PodInfo podInfo) {
    List<EnvVar> envVars = new ArrayList<>();
    podInfo.getConfiguration().forEach((k, v) -> {
      envVars.add(new EnvVarBuilder().withName(k).withValue(v).build());
    });

    return envVars;
  }

  public boolean startPod(PodInfo podInfo) {
    log.debug("Starting pod '{}'.", podInfo);

    Deployment deploymentInfo = new DeploymentBuilder()
        .withNewMetadata()
        .withName(podInfo.getName())
        .endMetadata()
        .withNewSpec()
        .withNewSelector()
        .addToMatchLabels("app", podInfo.getName())
        .endSelector()
        .withNewTemplate()
        .withNewMetadata()
        .addToLabels("app", podInfo.getName())
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName(podInfo.getName())
        .withImage(podInfo.getImage())
        .withImagePullPolicy("Always")
        .withEnv(getEnvVar(podInfo))
        .endContainer()
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();

    Deployment deployment = kc.apps().deployments()
        .inNamespace(podInfo.getNamespace()).create(deploymentInfo);

    return true;
  }

  public List<String> getNamespaces() {
    return kc.namespaces().list().getItems().stream().map(
            n -> n.getMetadata().getName()).toList().stream()
        .sorted().collect(Collectors.toList());
  }
}
