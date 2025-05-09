package esthesis.services.audit.impl;

import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Security;
import esthesis.core.common.entity.BaseEntity;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.common.paging.Pageable;
import esthesis.services.audit.impl.repository.AuditRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import org.instancio.Instancio;
import org.mockito.Mockito;

@ApplicationScoped
public class TestHelper {

	@Inject
	AuditRepository auditRepository;

	public AuditEntity makeAuditEntity(String message,
																		 Security.Category category,
																		 Security.Operation operation) {
		return Instancio.of(AuditEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
			.set(field(AuditEntity.class, "message"), message)
			.set(field(AuditEntity.class, "category"), category)
			.set(field(AuditEntity.class, "operation"), operation)
			.create();
	}

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

	public void clearDB() {
		auditRepository.deleteAll();
	}
}
