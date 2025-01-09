package esthesis.service.kubernetes.impl.service;

import esthesis.service.kubernetes.dto.DeploymentInfoDTO;
import esthesis.service.kubernetes.dto.SecretDTO;
import esthesis.service.kubernetes.dto.SecretEntryDTO;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscalerList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.V2AutoscalingAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.AutoscalingAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import io.fabric8.kubernetes.client.dsl.ServerSideApplicable;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class KubernetesServiceTest {

	@InjectMocks
	KubernetesService kubernetesService;

	@Mock
	KubernetesClient kc;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Mock secrets
		MixedOperation<Secret, SecretList, Resource<Secret>> mockSecrets = mock(MixedOperation.class);
		NonNamespaceOperation<Secret, SecretList, Resource<Secret>> mockInNamespaceSecrets = mock(NonNamespaceOperation.class);
		Resource<Secret> mockResourceSecrets = mock(Resource.class);
		ServerSideApplicable<Secret> mockServerSideSecrets = mock(ServerSideApplicable.class);

		when(kc.secrets()).thenReturn(mockSecrets);
		when(mockSecrets.inNamespace(anyString())).thenReturn(mockInNamespaceSecrets);
		when(mockInNamespaceSecrets.resource(any())).thenReturn(mockResourceSecrets);
		when(mockResourceSecrets.forceConflicts()).thenReturn(mockServerSideSecrets);
		when(mockServerSideSecrets.serverSideApply()).thenReturn(mock(Secret.class));

		// Mock apps and deployments
		AppsAPIGroupDSL mockApps = mock(AppsAPIGroupDSL.class);
		MixedOperation<Deployment, DeploymentList, RollableScalableResource<Deployment>> mockDeployments = mock(MixedOperation.class);
		NonNamespaceOperation<Deployment, DeploymentList, RollableScalableResource<Deployment>> mockInNamespaceDeployments = mock(NonNamespaceOperation.class);
		RollableScalableResource<Deployment> mockDeploymentResource = mock(RollableScalableResource.class);

		when(kc.apps()).thenReturn(mockApps);
		when(mockApps.deployments()).thenReturn(mockDeployments);
		when(mockDeployments.inNamespace(anyString())).thenReturn(mockInNamespaceDeployments);
		when(mockInNamespaceDeployments.resource(any())).thenReturn(mockDeploymentResource);
		when(mockInNamespaceDeployments.withName(anyString())).thenReturn(mockDeploymentResource);
		when(mockDeploymentResource.get()).thenReturn(null); // Simulate that the deployment does not exist.
		when(mockDeploymentResource.forceConflicts()).thenReturn(mockDeploymentResource);
		when(mockDeploymentResource.serverSideApply()).thenReturn(mock(Deployment.class));

		// Mock autoscaling
		AutoscalingAPIGroupDSL mockAutoscaling = mock(AutoscalingAPIGroupDSL.class);
		V2AutoscalingAPIGroupDSL mockAutoscalingV2 = mock(V2AutoscalingAPIGroupDSL.class);
		MixedOperation<HorizontalPodAutoscaler, HorizontalPodAutoscalerList, Resource<HorizontalPodAutoscaler>> mockHpa = mock(MixedOperation.class);
		NonNamespaceOperation<HorizontalPodAutoscaler, HorizontalPodAutoscalerList, Resource<HorizontalPodAutoscaler>> mockInNamespaceHpa = mock(NonNamespaceOperation.class);
		Resource<HorizontalPodAutoscaler> mockHpaResource = mock(Resource.class);

		when(kc.autoscaling()).thenReturn(mockAutoscaling);
		when(mockAutoscaling.v2()).thenReturn(mockAutoscalingV2);
		when(mockAutoscalingV2.horizontalPodAutoscalers()).thenReturn(mockHpa);
		when(mockHpa.inNamespace(anyString())).thenReturn(mockInNamespaceHpa);
		when(mockInNamespaceHpa.resource(any())).thenReturn(mockHpaResource);
		when(mockHpaResource.forceConflicts()).thenReturn(mockHpaResource);
		when(mockHpaResource.serverSideApply()).thenReturn(mock(HorizontalPodAutoscaler.class));

		// Mock namespaces
		MixedOperation<Namespace, NamespaceList, Resource<Namespace>> mockNamespaces = mock(MixedOperation.class);
		NamespaceList mockNamespaceList = mock(NamespaceList.class);
		Namespace mockNamespace = mock(Namespace.class);

		when(kc.namespaces()).thenReturn(mockNamespaces);
		when(mockNamespaces.list()).thenReturn(mockNamespaceList);
		when(mockNamespaceList.getItems()).thenReturn(List.of(mockNamespace));
		when(mockNamespace.getMetadata()).thenReturn(new ObjectMetaBuilder().withName("test-namespace").build());

	}

	@Test
	void createSecret() {
		// Arrange
		List<SecretEntryDTO> secretEntries = List.of(
			new SecretEntryDTO("test-name-1", "test-content-1", "test-path-1"),
			new SecretEntryDTO("test-name-2", "test-content-2", "test-path-2")
		);
		SecretDTO secretDTO = new SecretDTO("test-secret", secretEntries);

		// Act & Assert
		assertDoesNotThrow(() -> kubernetesService.createSecret(secretDTO, "test-namespace"));
	}

	@Test
	void scheduleDeployment() {
		// Arrange
		List<SecretEntryDTO> secretEntries = List.of(
			new SecretEntryDTO("test-name-1", "test-content-1", "test-path-1"),
			new SecretEntryDTO("test-name-2", "test-content-2", "test-path-2")
		);
		SecretDTO secretDTO = new SecretDTO("test-secret", secretEntries);

		DeploymentInfoDTO deploymentInfo = getDeploymentInfoDTO(secretDTO);

		// Act & Assert
		assertDoesNotThrow(() -> kubernetesService.scheduleDeployment(deploymentInfo));
	}

	private DeploymentInfoDTO getDeploymentInfoDTO(SecretDTO secretDTO) {
		DeploymentInfoDTO deploymentInfo = new DeploymentInfoDTO();
		deploymentInfo.setName("test-deployment");
		deploymentInfo.setEnv("test-env");
		deploymentInfo.setStatus(true);
		deploymentInfo.setImage("test-image");
		deploymentInfo.setEnvironment(Map.of("test-key", "test-value"));
		deploymentInfo.setCpuLimit("500m");
		deploymentInfo.setCpuRequest("100m");
		deploymentInfo.setMaxInstances(2);
		deploymentInfo.setMinInstances(1);
		deploymentInfo.setNamespace("test-namespace");
		deploymentInfo.setSecret(secretDTO);
		deploymentInfo.setVersion("0.0.1");
		return deploymentInfo;
	}

	@Test
	void getNamespaces() {
		// Act
		List<String> namespaces = kubernetesService.getNamespaces();

		// Assert
		assertFalse(namespaces.isEmpty());
	}

	@Test
	void isDeploymentNameAvailable() {
		// Act
		boolean result = kubernetesService.isDeploymentNameAvailable("test-deployment", "test-namespace");

		// Assert
		assertTrue(result);
	}
}
