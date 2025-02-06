package esthesis.services.security.impl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import esthesis.service.security.entity.PolicyEntity;
import esthesis.services.security.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SecurityPolicyServiceTest {

	@Inject
	SecurityPolicyService securityPolicyService;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	void find() {
		// Assert no policies exist.
		assertTrue(
			securityPolicyService.find(testHelper.makePageable(1, 10))
				.getContent()
				.isEmpty());

		// Perform a save operation for a new policy.
		securityPolicyService.saveNew(testHelper.makePolicyEntity("test policy"));

		// Assert policy can be found.
		assertFalse(
			securityPolicyService.find(testHelper.makePageable(0, 10))
				.getContent()
				.isEmpty());
	}

	@Test
	void findById() {
		// Perform a save operation for a new policy.
		String policyId =
			securityPolicyService.saveNew(testHelper.makePolicyEntity("test policy"))
				.getId()
				.toHexString();

		// Assert policy can be found.
		assertNotNull(securityPolicyService.findById(policyId));
	}

	@Test
	void deleteById() {
		// Perform a save operation for a new policy.
		String policyId =
			securityPolicyService.saveNew(testHelper.makePolicyEntity("test policy"))
				.getId()
				.toHexString();

		// Assert policy can be found.
		assertNotNull(securityPolicyService.findById(policyId));

		// Perform a delete operation for the given policy ID.
		securityPolicyService.deleteById(policyId);

		// Assert policy cannot be found.
		assertNull(securityPolicyService.findById(policyId));
	}

	@Test
	void saveNew() {
		// Perform a save operation for a new policy.
		String policyId =
			securityPolicyService.saveNew(
					new PolicyEntity(
						"test policy",
						"test description",
						"test rule"))
				.getId()
				.toHexString();

		// Assert policy was saved with correct values.
		PolicyEntity policy = securityPolicyService.findById(policyId);
		assertEquals("test policy", policy.getName());
		assertEquals("test description", policy.getDescription());
		assertEquals("test rule", policy.getRule());


	}

	@Test
	void saveUpdate() {
		// Perform a save operation for a new policy.
		String policyId =
			securityPolicyService.saveNew(testHelper.makePolicyEntity("test policy"))
				.getId()
				.toHexString();

		// Find the policy by ID.
		PolicyEntity policy = securityPolicyService.findById(policyId);

		// Perform an update operation for the given policy.
		policy.setName("updated name");
		policy.setDescription("updated description");
		policy.setRule("updated rule");
		securityPolicyService.saveUpdate(policy);

		// Assert policy was updated with correct values.
		policy = securityPolicyService.findById(policyId);
		assertEquals("updated name", policy.getName());
		assertEquals("updated description", policy.getDescription());
		assertEquals("updated rule", policy.getRule());
	}

	@Test
	void countAll() {
		// Assert count of all policies is 0.
		assertEquals(0, securityPolicyService.countAll());

		// Perform a save operation for a new policy.
		securityPolicyService.saveNew(testHelper.makePolicyEntity("test policy"));

		// Assert count of all policies is 1.
		assertEquals(1, securityPolicyService.countAll());
	}
}
