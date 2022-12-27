package esthesis.service.kubernetes.impl.service;

import esthesis.service.kubernetes.dto.PodInfoDTO;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.autoscaling.v2beta2.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.autoscaling.v2beta2.HorizontalPodAutoscalerBuilder;
import io.fabric8.kubernetes.api.model.autoscaling.v2beta2.MetricSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class KubernetesService {

  @Inject
  private KubernetesClient kc;

  private List<EnvVar> getEnvVar(PodInfoDTO podInfoDTO) {
    List<EnvVar> envVars = new ArrayList<>();
    podInfoDTO.getConfiguration().forEach((k, v) ->
        envVars.add(new EnvVarBuilder().withName(k).withValue(v).build())
    );

    return envVars;
  }

  public boolean schedulePod(PodInfoDTO podInfoDTO) {
    log.debug("Scheduling pod '{}'.", podInfoDTO);

    //@formatter:off
    // Create a deployment for the pod.
    Deployment deploymentInfo = new DeploymentBuilder()
      .withNewMetadata()
        .withName(podInfoDTO.getName())
      .endMetadata()
      .withNewSpec()
        .withNewSelector()
          .addToMatchLabels("app", podInfoDTO.getName())
        .endSelector()
        .withNewTemplate()
          .withNewMetadata()
            .addToLabels("app", podInfoDTO.getName())
          .endMetadata()
          .withNewSpec()
            .addNewContainer()
              .withName(podInfoDTO.getName())
              .withImage(podInfoDTO.getImage())
              .withImagePullPolicy("Always")
              .withEnv(getEnvVar(podInfoDTO))
              .withResources(
                  new ResourceRequirementsBuilder()
                      .addToRequests("cpu", Quantity.parse(podInfoDTO.getCpuRequest()))
                      .addToLimits("cpu", Quantity.parse(podInfoDTO.getCpuLimit()))
                      .build())
            .endContainer()
          .endSpec()
        .endTemplate()
      .endSpec()
    .build();
    // @formatter:on

    if (podInfoDTO.isStatus()) {
      kc.apps().deployments().inNamespace(podInfoDTO.getNamespace())
          .createOrReplace(deploymentInfo);
    } else {
      kc.apps().deployments().inNamespace(podInfoDTO.getNamespace())
          .delete(deploymentInfo);
    }

    //@formatter:off
    // Create a horizontal pod autoscaler for the pod.
    HorizontalPodAutoscaler horizontalPodAutoscaler = new HorizontalPodAutoscalerBuilder()
      .withNewMetadata().withName("hpa-" + podInfoDTO.getName())
        .endMetadata()
      .withNewSpec()
        .withNewScaleTargetRef()
          .withApiVersion("apps/v1")
          .withKind("Deployment")
          .withName(podInfoDTO.getName())
        .endScaleTargetRef()
        .withMinReplicas(podInfoDTO.getMinInstances())
        .withMaxReplicas(podInfoDTO.getMaxInstances())
        .addToMetrics(new MetricSpecBuilder()
            .withType("Resource")
            .withNewResource()
            .withName("cpu")
            .withNewTarget()
            .withType("Utilization")
            .withAverageUtilization(80)
            .endTarget()
            .endResource()
            .build())
        .withNewBehavior()
          .withNewScaleDown()
            .addNewPolicy()
              .withType("Pods")
              .withValue(4)
              .withPeriodSeconds(60)
            .endPolicy()
            .addNewPolicy()
              .withType("Percent")
              .withValue(10)
              .withPeriodSeconds(60)
            .endPolicy()
          .endScaleDown()
        .endBehavior()
      .endSpec()
    .build();
    //@formatter:on

    // Create or remove the pod, according to the requested pod status.
    if (podInfoDTO.isStatus()) {
      kc.autoscaling().v2beta2().horizontalPodAutoscalers().inNamespace(
          podInfoDTO.getNamespace()).createOrReplace(horizontalPodAutoscaler);
    } else {
      kc.autoscaling().v2beta2().horizontalPodAutoscalers().inNamespace(
          podInfoDTO.getNamespace()).delete(horizontalPodAutoscaler);
    }

    return true;
  }

  public List<String> getNamespaces() {
    return kc.namespaces().list().getItems().stream().map(
            n -> n.getMetadata().getName()).toList().stream()
        .sorted().toList();
  }
}
