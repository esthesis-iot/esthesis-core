package esthesis.services.audit.impl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.common.paging.Page;
import esthesis.services.audit.impl.TestHelper;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestTransaction
class AuditServiceTest {

	@Inject
	AuditService auditService;

	@Inject
	TestHelper testHelper;

	@Test
	void find() {
		// Insert 2 audit entities
		testHelper.persistAuditEntity();
		testHelper.persistAuditEntity();

		// Assert find all
		Page<AuditEntity> entityPage =
			auditService.find(testHelper.makePageable(0, 10), true);

		assertTrue(entityPage.getContent().size() >= 2);
	}

	@Test
	void findByIdOK() {
		// Insert and extract new audit id
		String auditId = testHelper.persistAuditEntity().getId().toString();

		AuditEntity entity = auditService.findById(auditId);

		// Assert entity was persisted
		assertNotNull(entity);
		assertEquals(auditId, entity.getId().toString());
	}

	@Test
	void findByIdNOK() {
		// Insert new audit entity
		testHelper.persistAuditEntity().getId().toString();

		// Find audit entity with non-existent id
		AuditEntity entity = auditService.findById(new ObjectId().toString());

		// Assert entity was not persisted
		assertNull(entity);

	}

	@Test
	void deleteByIdOK() {
		// Count the records before delete.
		long countBefore = auditService.countAll();

		// Insert and extract new audit id.
		String auditId = testHelper.persistAuditEntity().getId().toString();

		// Perform delete operation.
		boolean deleted = auditService.deleteById(auditId);

		// Assert entity was deleted.
		assertTrue(deleted);
		long countAfter = auditService.countAll();
		assertEquals(countBefore, countAfter);
	}

	@Test
	void deleteByIdNOK() {
		// Count the records before delete.
		long countBefore = auditService.countAll();

		// Insert a new audit entity.
		testHelper.persistAuditEntity();

		// Perform delete operation for a nonexistent id.
		boolean deleted = auditService.deleteById(new ObjectId().toString());

		// Assert entity was not deleted.
		assertFalse(deleted);
		long countAfter = auditService.countAll();
		assertTrue(countBefore < countAfter);
	}

	@Test
	void save() {
		// Perform a save operation.
		String id = auditService.save(testHelper.makeAuditEntity()).getId().toHexString();

		// Assert entity was persisted.
		assertNotNull(auditService.findById(id));
	}
}
