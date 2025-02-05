package esthesis.services.security.impl;

import esthesis.service.common.paging.Pageable;
import esthesis.service.security.entity.GroupEntity;
import esthesis.service.security.entity.PolicyEntity;
import esthesis.service.security.entity.RoleEntity;
import esthesis.service.security.entity.UserEntity;
import esthesis.services.security.impl.repository.SecurityGroupRepository;
import esthesis.services.security.impl.repository.SecurityPolicyRepository;
import esthesis.services.security.impl.repository.SecurityRoleRepository;
import esthesis.services.security.impl.repository.SecurityUserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.mockito.Mockito;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import static org.mockito.Mockito.when;

@ApplicationScoped
public class TestHelper {

	@Inject
	SecurityGroupRepository securityGroupRepository;
	@Inject
	SecurityPolicyRepository securityPolicyRepository;
	@Inject
	SecurityRoleRepository securityRoleRepository;
	@Inject
	SecurityUserRepository securityUserRepository;

	public void clearDatabase() {
		securityGroupRepository.deleteAll();
		securityPolicyRepository.deleteAll();
		securityRoleRepository.deleteAll();
		securityUserRepository.deleteAll();
	}

	/**
	 * Mock a Pageable object with the specified parameters.
	 *
	 * @param page The page number being requested.
	 * @param size The size of the page.
	 * @return The mocked Pageable object.
	 */
	public Pageable makePageable(int page, int size) {

		// Mock the request URI and parameters.
		UriInfo uriInfo = Mockito.mock(UriInfo.class);
		when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/find?page=" + page + "&size=" + size));
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

		Pageable pageable = new Pageable();
		pageable.setPage(page);
		pageable.setSize(size);
		pageable.setSort("");
		pageable.setUriInfo(uriInfo);
		return pageable;
	}

	public GroupEntity makeGroupEntity(String name) {
		GroupEntity groupEntity = new GroupEntity();
		groupEntity.setName(name);
		groupEntity.setDescription("test description");
		groupEntity.setRoles(new java.util.ArrayList<>());
		groupEntity.setPolicies(new java.util.ArrayList<>());
		return groupEntity;
	}

	public GroupEntity makeGroupEntity(String name, List<String> policies) {
		return makeGroupEntity(name).setPolicies(policies);
	}


	public PolicyEntity makePolicyEntity(String testPolicy) {
		PolicyEntity policyEntity = new PolicyEntity();
		policyEntity.setName(testPolicy);
		policyEntity.setDescription("test description");
		policyEntity.setRule("test rule");
		return policyEntity;
	}

	public PolicyEntity makePolicyEntity(String name, String rule) {
		return makePolicyEntity(name).setRule(rule);
	}

	public RoleEntity makeRoleEntity(String testRole) {
		RoleEntity roleEntity = new RoleEntity();
		roleEntity.setName(testRole);
		roleEntity.setDescription("test description");
		roleEntity.setPolicies(List.of("test policy"));
		return roleEntity;
	}

	public RoleEntity makeRoleEntity(String role, List<String> policies) {
		return makeRoleEntity(role).setPolicies(policies);
	}

	public UserEntity makeUserEntity(String testUser) {
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername(testUser);
		userEntity.setDescription("test description");
		userEntity.setFirstName("FirstName");
		userEntity.setLastName("test last name");
		userEntity.setGroups(List.of("test group"));
		userEntity.setPolicies(List.of("test policy"));
		userEntity.setLastName("LastName");
		userEntity.setEmail("test@email.eu");
		return userEntity;
	}

	public UserEntity makeUserEntity(String username, List<String> policies, List<String> groups) {
		return makeUserEntity(username).setPolicies(policies).setGroups(groups);
	}

	public Principal makePrincipal(String username) {
		return () -> username;
	}
}
