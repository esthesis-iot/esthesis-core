package esthesis.service.application.impl.service;

import esthesis.service.application.entity.ApplicationEntity;
import esthesis.service.application.impl.repository.ApplicationRepository;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class ApplicationServiceTest {

	@Inject
	ApplicationService applicationService;

	@Inject
	ApplicationRepository applicationRepository;

	@Mock
	UriInfo uriInfo;


	/**
	 * Helper method to create and persist an application entity.
	 */
	private ApplicationEntity createApplication(String name, boolean state, String token) {
		ApplicationEntity applicationEntity =
			new ApplicationEntity()
				.setName(name)
				.setState(state)
				.setToken(token)
				.setCreatedOn(Instant.now());
		applicationRepository.persist(applicationEntity);

		return applicationEntity;
	}

	/**
	 * Helper method to create a Pageable object with the specified parameters
	 */
	private Pageable getPageable(int page, int size, String sort) {
		Pageable pageable = new Pageable();
		pageable.setPage(page);
		pageable.setSize(size);
		pageable.setSort(sort); // Sorting field and direction (e.g., "name,asc")
		pageable.setUriInfo(uriInfo); // using mock uriInfo to simplify
		return pageable;
	}

	@BeforeEach
	void setupMocks() {
		// Initialize mocks
		MockitoAnnotations.openMocks(this);

		// Mock UriInfo methods
		when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/applications?page=0&size=10&sort=name,asc"));
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

	}

	@BeforeEach
	void clearDatabase() {
		applicationRepository.deleteAll();
	}

	@Test
	void find() {
		// Create test applications
		createApplication("test-application-1", true, "test-token-1");
		createApplication("test-application-2", true, "test-token-2");

		// Create test Pageable
		Pageable pageable = getPageable(0, 10, "name,asc");

		Page<ApplicationEntity> applications = applicationService.find(pageable);

		//Assert find all
		assertEquals(2, applications.getContent().size());

	}

	@Test
	void saveNew() {
		var newApplication =
			applicationService.saveNew(
				new ApplicationEntity()
					.setName("test-application")
					.setToken("test-token")
					.setState(true));

		// Assert new Id has been generated
		assertNotNull(newApplication.getId());

		// Assert it was persisted
		assertEquals(1, applicationRepository.findAll().stream().count());
	}

	@Test
	void saveUpdate() {

		// Persist new application
		ApplicationEntity application =
			applicationService.saveUpdate(
				new ApplicationEntity("test-application", "test-token", true, Instant.now())
			);

		// Assert application was persisted
		assertEquals(1, applicationRepository.findAll().stream().count());

		// change application values
		application.setName("test-application-update");
		application.setState(false);
		application.setToken("test-token-update");

		// Persist update
		applicationService.saveUpdate(application);

		List<ApplicationEntity> applications = applicationRepository.findAll().list();

		// Assert it was not duplicated
		assertEquals(1, applications.size());

		ApplicationEntity applicationUpdated = applications.getFirst();
		// Assert changes were persisted
		assertNotNull(applicationUpdated.getId());
		assertEquals("test-application-update", applicationUpdated.getName());
		assertEquals(false, applicationUpdated.getState());
		assertEquals("test-token-update", applicationUpdated.getToken());

	}


	@Test
	void deleteByIdOK() {
		// Create test application
		ApplicationEntity application = createApplication("test-application", true, "test-token");

		// Perform delete
		boolean deleted = applicationService.deleteById(application.getId().toString());

		// Assert positive deletion indication
		assertTrue(deleted);

		//Assert it was removed from db
		assertEquals(0, applicationRepository.findAll().stream().count());
	}

	@Test
	void deleteByIdNOK() {
		// Create test application
		createApplication("test-application", true, "test-token");

		// Perform delete with nonexistent object id
		boolean deleted = applicationService.deleteById(new ObjectId().toString());

		// Assert negative deletion indication
		assertFalse(deleted);

		// Assert nothing was removed from db
		assertEquals(1, applicationRepository.findAll().stream().count());
	}

	@Test
	void findByIdOK() {
		// Create test application
		String applicationId = createApplication("test-application", true, "test-token").getId().toString();

		ApplicationEntity application = applicationService.findById(applicationId);

		// Assert application was found
		assertNotNull(application);
	}

	@Test
	void findByIdNOK() {
		// Create test application
		createApplication("test-application", true, "test-token");

		// Assert won't find any application with nonexistent id
		assertNull(applicationService.findById(new ObjectId().toString()));

		// Assert won't accept invalid Object id
		assertThrows(IllegalArgumentException.class, () -> applicationService.findById("invalid-object-id"));

	}

	@Test
	void isTokenValidOK() {
		// Create test application with state enabled
		createApplication("test-application-1", true, "test-token-1");
		assertTrue(applicationService.isTokenValid("test-token-1"));
	}

	@Test
	void isTokenValidNOK() {
		// Create test application with state disabled
		createApplication("test-application-2", false, "test-token-2");

		assertFalse(applicationService.isTokenValid("test-token-2"));
		assertFalse(applicationService.isTokenValid("test-token-nonexistent"));
	}
}
