package esthesis.services.audit.impl;

import static org.instancio.Select.all;
import static org.instancio.Select.field;

import esthesis.common.entity.BaseEntity;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.services.audit.impl.repository.AuditRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.instancio.Instancio;

@ApplicationScoped
public class TestHelper {

	@Inject
	AuditRepository auditRepository;

	public AuditEntity makeAuditEntity() {
		return Instancio.of(AuditEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
			.create();
	}

	public AuditEntity persistAuditEntity() {
		AuditEntity auditEntity = makeAuditEntity();
		auditRepository.persist(auditEntity);

		return auditEntity;
	}

	public AuditEntity persistAuditEntityForUser(String username) {
		AuditEntity auditEntity =
			Instancio.of(AuditEntity.class)
				.ignore(all(field(BaseEntity.class, "id")))
				.set(field(AuditEntity.class, "createdBy"), username)
				.create();
		auditRepository.persist(auditEntity);

		return auditEntity;
	}

}
