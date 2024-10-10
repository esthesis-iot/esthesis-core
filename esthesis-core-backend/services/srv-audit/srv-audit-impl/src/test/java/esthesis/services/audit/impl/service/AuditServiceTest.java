package esthesis.services.audit.impl.service;

import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.common.paging.Page;
import esthesis.services.audit.impl.TestHelper;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
			auditService.find(testHelper.makePageable(0,10,"name,asc"), true);

		assertEquals(2, entityPage.getContent().size());
	}

	@Test
	void findByIdOK() {
		// Insert and extract new audit id
		String auditId = testHelper.persistAuditEntity().getId().toString();

		AuditEntity entity = auditService.findById(auditId);

		// Assert entity was persisted
		assertNotNull(entity);
		assertEquals(auditId, entity.getId().toString());
		assertEquals(1, testHelper.findAllEntities().stream().count());

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
		// Insert and extract new audit id
		String auditId = testHelper.persistAuditEntity().getId().toString();

	 // Perform delete operation
	 boolean deleted =	auditService.deleteById(auditId);

		// Assert entity was deleted
		assertTrue(deleted);
		assertEquals(0, testHelper.findAllEntities().stream().count());
	}

	@Test
	void deleteByIdNOK() {
		// Insert a new audit entity
		testHelper.persistAuditEntity().getId().toString();

		// Perform delete operation for a nonexistent id
		boolean deleted =	auditService.deleteById(new ObjectId().toString());

		// Assert entity was not deleted
		assertFalse(deleted);
		assertEquals(1, testHelper.findAllEntities().stream().count());
	}

	@Test
	void save() {
		// Perform a save operation
		auditService.save(testHelper.makeAuditEntity());

		// Assert entity was persisted
		assertEquals(1, testHelper.findAllEntities().stream().count());
	}
}
