package esthesis.service.kubernetes.impl.producer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KubernetesClientProducerTest {

	KubernetesClientProducer kubernetesClientProducer;

	@Test
	void kubernetesClient() {
		kubernetesClientProducer = new KubernetesClientProducer();
		assertNotNull(kubernetesClientProducer.kubernetesClient());
	}
}
