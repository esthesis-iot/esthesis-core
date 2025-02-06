package esthesis.service.application.impl.service;

import esthesis.service.application.entity.ApplicationEntity;
import esthesis.service.application.impl.repository.ApplicationRepository;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
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
	 * Helper method to create a Pageable object with the specified parameters.
	 *
	 * @param page Current page.
	 * @param size Size of the page.
	 * @param sort Sort order.
	 * @return The mocked Pageable object.
	 */
	private Pageable getPageable(int page, int size, String sort) {
		Pageable pageable = new Pageable();
		pageable.setPage(page);
		pageable.setSize(size);
		pageable.setSort(sort);
		pageable.setUriInfo(uriInfo);
		return pageable;
	}

	@BeforeEach
	void setupMocks() {
		// Initialize mocks.
		MockitoAnnotations.openMocks(this);

		// Mock Pageable UriInfo methods.
		when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/applications?page=0&size=10&sort=name,asc"));
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

	}

	@BeforeEach
	void clearDatabase() {
		applicationRepository.deleteAll();
	}

	@Test
	void find() {
		// Perform the save operation for a new application.
		applicationService.saveNew(
			new ApplicationEntity()
				.setName("test-application")
				.setToken("test-token")
				.setState(true));

		// Create a test pageable.
		Pageable pageable = getPageable(0, 10, "name,asc");

		Page<ApplicationEntity> applications = applicationService.find(pageable);

		//Assert find all has results.
		assertFalse(applications.getContent().isEmpty());

	}

	@Test
	void saveNew() {
		// Perform the save operation for a new application.
		String applicationId =
			applicationService.saveNew(
					new ApplicationEntity()
						.setName("test-application")
						.setToken("test-token")
						.setState(true))
				.getId()
				.toHexString();

		// Assert application was persisted with correct values.
		ApplicationEntity application = applicationService.findById(applicationId);
		assertEquals("test-application", application.getName());
		assertEquals("test-token", application.getToken());
		assertEquals(true, application.getState());

	}

	@Test
	void saveUpdate() {

		// Perform the save operation for a new application.
		String applicationId =
			applicationService.saveNew(
					new ApplicationEntity()
						.setName("test-application")
						.setToken("test-token")
						.setState(true))
				.getId()
				.toHexString();

		// Perform the update operation.
		ApplicationEntity application = applicationService.findById(applicationId);

		application.setName("test-application-update");
		application.setState(false);
		application.setToken("test-token-update");
		applicationService.saveUpdate(application);

		// Assert application was update with the correct values.
		ApplicationEntity applicationUpdated = applicationService.findById(applicationId);

		assertEquals("test-application-update", applicationUpdated.getName());
		assertEquals(false, applicationUpdated.getState());
		assertEquals("test-token-update", applicationUpdated.getToken());

	}


	@Test
	void deleteByIdOK() {
		// Perform the save operation for a new application.
		String applicationId =
			applicationService.saveNew(
					new ApplicationEntity()
						.setName("test-application")
						.setToken("test-token")
						.setState(true))
				.getId()
				.toHexString();

		// Assert application exists.
		assertNotNull(applicationService.findById(applicationId));

		// Perform the delete operation.
		applicationService.deleteById(applicationId);

		// Assert application was deleted.
		assertNull(applicationService.findById(applicationId));
	}

	@Test
	void deleteByIdNOK() {
		// Perform the save operation for a new application.
		String applicationId =
			applicationService.saveNew(
					new ApplicationEntity()
						.setName("test-application")
						.setToken("test-token")
						.setState(true))
				.getId()
				.toHexString();

		// Perform the delete operation of a non-existent application.
		applicationService.deleteById(new ObjectId().toString());

		// Assert application was not deleted.
		assertNotNull(applicationService.findById(applicationId));
	}

	@Test
	void findByIdOK() {
		// Perform the save operation for a new application.
		String applicationId =
			applicationService.saveNew(
					new ApplicationEntity()
						.setName("test-application")
						.setToken("test-token")
						.setState(true))
				.getId()
				.toHexString();

		// Assert application can be found.
		assertNotNull(applicationService.findById(applicationId));
	}

	@Test
	void findByIdNOK() {
		// Perform the save operation for a new application.
		applicationService.saveNew(
			new ApplicationEntity()
				.setName("test-application")
				.setToken("test-token")
				.setState(true));

		// Assert application cannot be found for non-existent id.
		assertNull(applicationService.findById(new ObjectId().toString()));

		// Assert won't accept invalid Object id.
		assertThrows(IllegalArgumentException.class, () -> applicationService.findById("invalid-object-id"));

	}

	@Test
	void isTokenValidOK() {
		// Perform the save operation for a new application with state enabled.
		applicationService.saveNew(
			new ApplicationEntity()
				.setName("test-application")
				.setToken("test-token")
				.setState(true));

		// Assert application token is valid.
		assertTrue(applicationService.isTokenValid("test-token"));

	}

	@Test
	void isTokenValidNOK() {
		// Perform the save operation for a new application with state disabled.
		applicationService.saveNew(
			new ApplicationEntity()
				.setName("test-application")
				.setToken("test-token-disabled")
				.setState(false));

		// Assert application token is not valid.
		assertFalse(applicationService.isTokenValid("test-token-disabled"));
	}
}
