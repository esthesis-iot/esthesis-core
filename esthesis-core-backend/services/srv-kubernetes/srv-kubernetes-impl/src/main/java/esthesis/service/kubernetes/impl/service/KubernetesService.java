package esthesis.service.kubernetes.impl.service;

import static esthesis.core.common.AppConstants.Security.Category.KUBERNETES;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static java.nio.charset.StandardCharsets.UTF_8;

import esthesis.service.kubernetes.dto.DeploymentInfoDTO;
import esthesis.service.kubernetes.dto.SecretDTO;
import esthesis.service.security.annotation.ErnPermission;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscalerBuilder;
import io.fabric8.kubernetes.api.model.autoscaling.v2.MetricSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to interact with Kubernetes.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class KubernetesService {

	private static final String SECRET_MOUNT_LOCATION = "/etc/esthesis/secrets";
	private static final String SECRET_VOLUME_SUFFIX = "-secret-volume";

	@Inject
	KubernetesClient kc;

	/**
	 * Generates a list of environmental variables out of a pod definition.
	 *
	 * @param deploymentInfoDTO The pod definition.
	 * @return The list of environmental variables.
	 */
	private List<EnvVar> getEnvVar(DeploymentInfoDTO deploymentInfoDTO) {
		List<EnvVar> envVars = new ArrayList<>();
		deploymentInfoDTO.getEnvironment().forEach((k, v) ->
			envVars.add(new EnvVarBuilder().withName(k).withValue(v).build())
		);

		return envVars;
	}

	/**
	 * Creates or updates a secret.
	 *
	 * @param secretDTO The secret to create or update.
	 */
	@ErnPermission(category = KUBERNETES, operation = CREATE)
	public void createSecret(SecretDTO secretDTO, String namespace) {
		if (secretDTO == null || secretDTO.getEntries() == null || secretDTO.getEntries().isEmpty()) {
			return;
		}

		SecretBuilder secretBuilder = new SecretBuilder();
		secretBuilder.withNewMetadata().withName(secretDTO.getName()).endMetadata();
		secretDTO.getEntries().forEach(secret ->
			secretBuilder.addToData(secret.getName(),
				Base64.getEncoder().encodeToString(secret.getContent().getBytes(UTF_8))));
		log.debug("Creating secret '{}'.", secretBuilder.build());
		kc.secrets().inNamespace(namespace).resource(secretBuilder.build()).forceConflicts()
			.serverSideApply();

	}

	/**
	 * Schedules a Kubernetes Deployment.
	 *
	 * @param deploymentInfoDTO The deployment information to deploy.
	 * @return True if the deployment was successful, false otherwise.
	 */
	@ErnPermission(category = KUBERNETES, operation = CREATE)
	public boolean scheduleDeployment(DeploymentInfoDTO deploymentInfoDTO) {
		log.debug("Scheduling deployment '{}'.", deploymentInfoDTO);

		//@formatter:off
    // Create a deployment specification.
    DeploymentBuilder deploymentBuilder = new DeploymentBuilder()
      .withNewMetadata()
        .withName(deploymentInfoDTO.getName())
      .endMetadata()
      .withNewSpec()
        .withNewSelector()
          .addToMatchLabels("app", deploymentInfoDTO.getName())
        .endSelector()
        .withNewTemplate()
          .withNewMetadata()
            .addToLabels("app", deploymentInfoDTO.getName())
          .endMetadata()
          .withNewSpec()
            .addNewContainer()
              .withName(deploymentInfoDTO.getName())
              .withImage(deploymentInfoDTO.getImage())
              .withImagePullPolicy("Always")
              .withEnv(getEnvVar(deploymentInfoDTO))
              .withResources(
                  new ResourceRequirementsBuilder()
                      .addToRequests("cpu", Quantity.parse(deploymentInfoDTO.getCpuRequest()))
                      .addToLimits("cpu", Quantity.parse(deploymentInfoDTO.getCpuLimit()))
                      .build())
            .endContainer()
          .endSpec()
        .endTemplate()
      .endSpec();

		// Add secrets to the deployment definition.
		if (deploymentInfoDTO.getSecret() != null && deploymentInfoDTO.getSecret().getEntries() != null
			&& !deploymentInfoDTO.getSecret().getEntries().isEmpty()) {
			createSecret(deploymentInfoDTO.getSecret(), deploymentInfoDTO.getNamespace());
			String volumeName = deploymentInfoDTO.getName() + KubernetesService.SECRET_VOLUME_SUFFIX;
				deploymentBuilder.editSpec().editTemplate()
				.editSpec()
					.addNewVolumeLike(
						new VolumeBuilder().withName(volumeName)
							.withSecret(new SecretVolumeSourceBuilder()
								.withSecretName(deploymentInfoDTO.getSecret().getName())
								.build())
							.build())
					.endVolume()
				.editContainer(0).addNewVolumeMountLike(
					new VolumeMountBuilder()
						.withName(volumeName)
						.withMountPath(KubernetesService.SECRET_MOUNT_LOCATION).build()
				).endVolumeMount().endContainer()
				.endSpec().endTemplate().endSpec();
		}
		// @formatter:on
		// Push (or delete) the deployment.
		if (deploymentInfoDTO.isStatus()) {
			kc.apps().deployments().inNamespace(deploymentInfoDTO.getNamespace())
				.resource(deploymentBuilder.build()).forceConflicts().serverSideApply();
		} else {
			kc.apps().deployments().inNamespace(deploymentInfoDTO.getNamespace())
				.resource(deploymentBuilder.build()).delete();
			// Delete secrets.
			if (deploymentInfoDTO.getSecret() != null) {
				kc.secrets().inNamespace(deploymentInfoDTO.getNamespace())
					.withName(deploymentInfoDTO.getSecret().getName()).delete();
			}
		}

		//@formatter:off
    // Create an HPA definition.
    HorizontalPodAutoscaler horizontalPodAutoscaler = new HorizontalPodAutoscalerBuilder()
      .withNewMetadata().withName("hpa-" + deploymentInfoDTO.getName())
        .endMetadata()
      .withNewSpec()
        .withNewScaleTargetRef()
          .withApiVersion("apps/v1")
          .withKind("Deployment")
          .withName(deploymentInfoDTO.getName())
        .endScaleTargetRef()
        .withMinReplicas(deploymentInfoDTO.getMinInstances())
        .withMaxReplicas(deploymentInfoDTO.getMaxInstances())
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

		// Push the HPA.
		if (deploymentInfoDTO.isStatus()) {
			kc.autoscaling().v2().horizontalPodAutoscalers().inNamespace(
					deploymentInfoDTO.getNamespace()).resource(horizontalPodAutoscaler).forceConflicts()
				.serverSideApply();
		} else {
			kc.autoscaling().v2().horizontalPodAutoscalers().inNamespace(
				deploymentInfoDTO.getNamespace()).resource(horizontalPodAutoscaler).delete();
		}

		return true;
	}

	/**
	 * Returns the list of namespaces.
	 *
	 * @return The list of namespaces.
	 */
	@ErnPermission(category = KUBERNETES, operation = READ)
	public List<String> getNamespaces() {
		return kc.namespaces().list().getItems().stream().map(
				n -> n.getMetadata().getName()).toList().stream()
			.sorted().toList();
	}

	/**
	 * Check if a deployment with the given name exists.
	 *
	 * @param name      The deployment name to check.
	 * @param namespace The namespace to check.
	 * @return True if the deployment name is available, false otherwise.
	 */
	@ErnPermission(category = KUBERNETES, operation = READ)
	public boolean isDeploymentNameAvailable(String name, String namespace) {
		return kc.apps().deployments().inNamespace(namespace).withName(name).get() == null;
	}
}
