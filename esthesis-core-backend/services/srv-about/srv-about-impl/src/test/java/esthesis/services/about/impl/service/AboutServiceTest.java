package esthesis.services.about.impl.service;


import esthesis.service.about.dto.AboutGeneralDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@QuarkusTest
class AboutServiceTest {

	@Inject
	AboutService aboutService;

	@Test
	void testGetGeneralInfo() {
		// Call the getGeneralInfo() method
		AboutGeneralDTO about = aboutService.getGeneralInfo();

		// Verify the results
		assertNotNull(about);
		assertNotNull(about.getGitBuildTime());
		assertNotNull(about.getGitCommitId());
		assertNotNull(about.getGitCommitIdAbbrev());
		assertNotNull(about.getGitVersion());
	}
}
