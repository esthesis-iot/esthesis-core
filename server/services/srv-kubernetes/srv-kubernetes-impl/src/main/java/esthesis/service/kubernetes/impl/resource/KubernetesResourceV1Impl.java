package esthesis.service.kubernetes.impl.resource;

import esthesis.service.kubernetes.resource.KubernetesResourceV1;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class KubernetesResourceV1Impl implements KubernetesResourceV1 {

  @Inject
  private KubernetesClient kc;

  @Override
  public Response startPod() {
    // TODO Auto-generated method stub
    System.out.println("hello world!");

    System.out.println(kc);

    PodList pods = kc.pods().inNamespace("esthesis").list();
    pods.getItems().forEach(pod -> {
      System.out.println(pod.getMetadata().getName());
    });

    Pod pod = new Pod();
    PodSpec podSpec = new PodSpec();

    Namespace ns = new NamespaceBuilder().withNewMetadata()
        .withName("test123").addToLabels("this", "rocks")
        .endMetadata().build();
    System.out.println("Created namespace: " + kc.namespaces().create(ns));

    return Response.ok("Hello world!").build();
  }
}
