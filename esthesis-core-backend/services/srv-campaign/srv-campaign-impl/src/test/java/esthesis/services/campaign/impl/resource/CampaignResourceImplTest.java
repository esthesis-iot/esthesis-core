package esthesis.services.campaign.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.resource.CampaignResource;
import esthesis.service.device.resource.DeviceResource;
import esthesis.services.campaign.impl.TestHelper;
import esthesis.services.campaign.impl.service.CampaignService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static esthesis.core.common.AppConstants.Campaign.State.CREATED;
import static esthesis.core.common.AppConstants.Campaign.State.PAUSED_BY_USER;
import static esthesis.core.common.AppConstants.Campaign.State.RUNNING;
import static esthesis.core.common.AppConstants.Campaign.State.TERMINATED_BY_WORKFLOW;
import static esthesis.core.common.AppConstants.Campaign.Type.PROVISIONING;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(CampaignResource.class)
class CampaignResourceImplTest {

	@Inject
	TestHelper testHelper;

	@Inject
	CampaignService campaignService;

	@InjectMock
	@RestClient
	DeviceResource deviceResource;

	@BeforeEach
	void clearDatabase() {
		testHelper.clearDatabase();

		when(deviceResource.findByHardwareIds(anyString()))
			.thenReturn(List.of(testHelper.makeDeviceEntity("test-hardwareId")));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void find() {
		campaignService.saveNew(
			testHelper.makeCampaignEntity(
				"test-campaign-new",
				"test description",
				PROVISIONING,
				CREATED));

		given()
			.contentType("application/json")
			.when().get("/v1/find?page=0&size=10&sort=createdOn,desc")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.size()", equalTo(1));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void save() {
		CampaignEntity campaign = testHelper.makeCampaignEntity(
			"test-campaign-new",
			"test description",
			PROVISIONING,
			CREATED);

		given()
			.contentType(ContentType.JSON)
			.body(campaign)
			.when().post("/v1")
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findById() {
		CampaignEntity campaign = campaignService.saveNew(
			testHelper.makeCampaignEntity(
				"test-campaign-new",
				"test description",
				PROVISIONING,
				CREATED));

		given()
			.when().get("/v1/" + campaign.getId().toHexString())
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("id", equalTo(campaign.getId().toHexString()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void start() {
		CampaignEntity campaign = campaignService.saveNew(
			testHelper.makeCampaignEntity(
				"test-campaign-new",
				"test description",
				PROVISIONING,
				CREATED));


		given()
			.when().get("/v1/" + campaign.getId().toHexString() + "/start")
			.then()
			.log().all()
			.statusCode(204);

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void resume() {
		CampaignEntity campaign = campaignService.saveNew(
			testHelper.makeCampaignEntity(
				"test-campaign-new",
				"test description",
				PROVISIONING,
				PAUSED_BY_USER));


		given()
			.when().get("/v1/" + campaign.getId().toHexString() + "/resume")
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void replay() {
		CampaignEntity campaign = campaignService.saveNew(
			testHelper.makeCampaignEntity(
				"test-campaign-new",
				"test description",
				PROVISIONING,
				TERMINATED_BY_WORKFLOW));


		// Verify a new campaign was created.
		given()
			.when().get("/v1/" + campaign.getId().toHexString() + "/replay")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("id", not(campaign.getId()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void terminate() {
		CampaignEntity campaign = campaignService.saveNew(
			testHelper.makeCampaignEntity(
				"test-campaign-new",
				"test description",
				PROVISIONING,
				CREATED).
				setProcessInstanceId(null));


		given()
			.when().get("/v1/" + campaign.getId().toHexString() + "/terminate")
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getCampaignStats() {

		CampaignEntity campaign = campaignService.saveNew(
			testHelper.makeCampaignEntity(
				"test-campaign-new",
				"test description",
				PROVISIONING,
				RUNNING));

		given()
			.when().get("/v1/" + campaign.getId().toHexString() + "/stats")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("state", is(RUNNING.name()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void delete() {
		CampaignEntity campaign = campaignService.saveNew(
			testHelper.makeCampaignEntity(
				"test-campaign-new",
				"test description",
				PROVISIONING,
				RUNNING));

		given()
			.when().delete("/v1/" + campaign.getId().toHexString())
			.then()
			.statusCode(200);

		given()
			.when().get("/v1/" + campaign.getId().toHexString())
			.then()
			.statusCode(204);
	}
}
