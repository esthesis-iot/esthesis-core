package esthesis.services.audit.impl.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Transactional
class AuditServiceTest {

	@Inject
	AuditService auditService;

	@Test
	void testFind() {
//		Page<AuditEntity> auditEntityPage = auditService.find(Pageable.empty());
//		assertEquals(10, auditEntityPage.getContent().size());
	}

}
