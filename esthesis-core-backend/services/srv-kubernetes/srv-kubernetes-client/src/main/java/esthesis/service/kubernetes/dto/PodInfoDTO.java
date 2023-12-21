package esthesis.service.kubernetes.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A DTO for the information required to deploy a pod.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class PodInfoDTO {

	// The name of the pod.
	private String name;

	// The container image to use for the pod.
	private String image;

	// The version of the container image to use for the pod.
	private String version;

	// The namespace to deploy the pod into.
	private String namespace;

	// The number of minimum and maximum instances of the pod to deploy.
	private int minInstances;
	private int maxInstances;

	// The amount of memory and CPU to request and limit for the pod.
	private String cpuRequest;
	private String cpuLimit;

	// If true, the pod will be created or updated. If false, the pod will be deleted.
	private boolean status;

	// The environment variables to set in the pod.
	private String env;

	// The secret to create for this pod.
	private SecretDTO secret;

	// Configuration options for the pod.
	private Map<String, String> configuration;
}
