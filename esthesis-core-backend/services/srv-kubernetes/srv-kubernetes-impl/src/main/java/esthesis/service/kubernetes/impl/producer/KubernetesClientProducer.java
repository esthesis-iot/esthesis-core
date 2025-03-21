package esthesis.service.kubernetes.impl.producer;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

/**
 * Produces a KubernetesClient instance to connect to a Kubernetes cluster.
 */
@Singleton
public class KubernetesClientProducer {

	private final KubernetesClientBuilder builder = new KubernetesClientBuilder();

	@Produces
	public KubernetesClient kubernetesClient() {
		return builder.build();
	}
}
