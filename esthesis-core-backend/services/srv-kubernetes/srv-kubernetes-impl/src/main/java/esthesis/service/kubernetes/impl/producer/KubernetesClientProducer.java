package esthesis.service.kubernetes.impl.producer;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@Singleton
public class KubernetesClientProducer {

	@Produces
	public KubernetesClient kubernetesClient() {

		return new DefaultKubernetesClient();
	}
}
