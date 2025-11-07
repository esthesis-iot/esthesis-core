package esthesis.service.kubernetes.impl.producer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for KubernetesClientProducer, testing Kubernetes client producer functionality.
 */
class KubernetesClientProducerTest {

	KubernetesClientProducer kubernetesClientProducer;

	@Test
	void kubernetesClient() {
		kubernetesClientProducer = new KubernetesClientProducer();
		assertNotNull(kubernetesClientProducer.kubernetesClient());
	}
}
