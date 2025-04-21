package esthesis.services.campaign.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.campaign.resource.CampaignSystemResource;
import esthesis.services.campaign.impl.TestHelper;
import esthesis.services.campaign.impl.service.CampaignService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static esthesis.core.common.AppConstants.Campaign.State.TERMINATED_BY_USER;
import static esthesis.core.common.AppConstants.Campaign.Type.PROVISIONING;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@TestHTTPEndpoint(CampaignSystemResource.class)
class CampaignSystemResourceImplTest {

	@Inject
	TestHelper testHelper;

	@Inject
	CampaignService campaignService;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void getStats() {
		campaignService.saveNew(
			testHelper.makeCampaignEntity(
				"test-campaign-new",
				"test description",
				PROVISIONING,
				TERMINATED_BY_USER));

		given()
			.when().get("/v1/system/general?lastCampaigns=1")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("$", hasSize(1));
	}
}
