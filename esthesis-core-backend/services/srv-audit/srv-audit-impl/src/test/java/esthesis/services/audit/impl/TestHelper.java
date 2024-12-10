package esthesis.services.audit.impl;

import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

import esthesis.core.common.entity.BaseEntity;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.common.paging.Pageable;
import esthesis.services.audit.impl.repository.AuditRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.instancio.Instancio;
import org.mockito.Mockito;

import java.net.URI;

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

	/**
	 * Helper method to create a Pageable object with the specified parameters
	 */
	public Pageable makePageable(int page, int size, String sort) {

		// Create a mock of UriInfo
		UriInfo uriInfo = Mockito.mock(UriInfo.class);

		// Define the behavior of the mock
		when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/find?page=0&size=10"));
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

		Pageable pageable = new Pageable();
		pageable.setPage(page);
		pageable.setSize(size);
		pageable.setSort(sort); // Sorting field and direction (e.g., "name,asc")
		pageable.setUriInfo(uriInfo);
		return pageable;
	}

	public PanacheQuery<AuditEntity> findAllEntities() {
		return auditRepository.findAll();
	}

	public void clearDB() {
		auditRepository.deleteAll();
	}
}
