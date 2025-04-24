package esthesis.services.dashboard.impl.job;

import esthesis.core.common.AppConstants;
import esthesis.service.about.resource.AboutSystemResource;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.audit.resource.AuditSystemResource;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.resource.CampaignSystemResource;
import esthesis.service.common.paging.Page;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.security.dto.StatsDTO;
import esthesis.service.security.resource.SecurityResource;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsSystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.service.DashboardService;
import esthesis.util.redis.RedisUtils;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.server.jaxrs.SseEventSinkImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_GEO_LAT;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_GEO_LON;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class DashboardUpdateJobTest {

	@Inject
	DashboardUpdateJobFactory dashboardUpdateJobFactory;

	DashboardUpdateJob dashboardUpdateJob;

	@Inject
	DashboardService dashboardService;

	@Inject
	TestHelper testHelper;

	@InjectMock
	SecurityIdentity securityIdentity;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SecurityResource securityResource;

	// Mocked user ID.
	ObjectId userId = new ObjectId();

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	DeviceSystemResource deviceSystemResource;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SettingsSystemResource settingsSystemResource;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SecuritySystemResource securitySystemResource;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	AboutSystemResource aboutSystemResource;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	AuditSystemResource auditSystemResource;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	CampaignSystemResource campaignSystemResource;


	@InjectMock
	RedisUtils redisUtils;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		// Mock the security identity for getting the current user.
		when(securityIdentity.getPrincipal())
			.thenReturn(testHelper.makePrincipal("test-user"));
		when(securityResource.findUserByUsername("test-user"))
			.thenReturn(testHelper.makeUser("test-user", userId));

		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock settings resource.
		when(settingsSystemResource.findByName(any())).thenReturn(new SettingEntity("test", "test"));

		// Mock the About GeneralInfo response.
		when(aboutSystemResource.getGeneralInfo()).thenReturn(testHelper.makeAboutGeneral());

		// Arrange and mock the device last seen stats.
		when(deviceSystemResource.getDeviceStats()).thenReturn(testHelper.makeDeviceLastSeenStats());

		// Arrange and mock the device total stats.
		when(deviceSystemResource.getDeviceTotalsStats()).thenReturn(testHelper.makeDeviceTotalStats());

		// Mock the find audit response.
		Page<AuditEntity> auditEntityPage = new Page<>();
		auditEntityPage.setContent(List.of(new AuditEntity().setCreatedBy("test-user").setMessage("test-message")));
		when(auditSystemResource.find(anyInt())).thenReturn(auditEntityPage);

		// Mock the campaign stats response.
		CampaignStatsDTO running = new CampaignStatsDTO().setState(AppConstants.Campaign.State.RUNNING);
		CampaignStatsDTO pausedByUser = new CampaignStatsDTO().setState(AppConstants.Campaign.State.PAUSED_BY_USER);
		CampaignStatsDTO pausedByWorkflow = new CampaignStatsDTO().setState(AppConstants.Campaign.State.PAUSED_BY_WORKFLOW);
		CampaignStatsDTO terminatedByUser = new CampaignStatsDTO().setState(AppConstants.Campaign.State.TERMINATED_BY_USER);
		CampaignStatsDTO terminatedByWorkflow = new CampaignStatsDTO().setState(AppConstants.Campaign.State.TERMINATED_BY_WORKFLOW);
		when(campaignSystemResource.getStats(anyInt()))
			.thenReturn(List.of(running, pausedByUser, pausedByWorkflow, terminatedByUser, terminatedByWorkflow));

		// Mock the finding of hardware IDs by tag IDs.
		when(deviceSystemResource.findByTagIds(anyString())).thenReturn(List.of("test-hardware"));

		// Mock the Redis cache to return the expected data.
		when(redisUtils.getFromHash(any(), anyString(), anyString())).thenReturn("10");

		// Mock the lat and lon settings.
		when(settingsSystemResource.findByName(DEVICE_GEO_LAT)).thenReturn(new SettingEntity("lat", "lat"));
		when(settingsSystemResource.findByName(DEVICE_GEO_LON)).thenReturn(new SettingEntity("lon", "long"));

		// Mock the latest devices.
		when(deviceSystemResource.getLatestDevices(anyInt()))
			.thenReturn(List.of(testHelper.makeCoreDevice("test-hardware")));

		// Mock security stats response.
		StatsDTO stats = StatsDTO.builder().users(1L).roles(1L).audits(1L).policies(1L).build();
		when(securitySystemResource.stats()).thenReturn(stats);

		// Persist a dashboard to test against.
		DashboardEntity dashboard = dashboardService.saveNew(testHelper.makeDashboard("test dashboard"));

		// Create a dashboard update job for the persisted dashboard.
		dashboardUpdateJob = dashboardUpdateJobFactory.create(
			"subscriptionId",
			dashboard.getId().toHexString(),
			mock(SseEventSinkImpl.class));



	}

	@Test
	void destroy() {
		assertDoesNotThrow(() -> dashboardUpdateJob.destroy());
	}

	@Test
	void execute() {

		assertDoesNotThrow(() -> dashboardUpdateJob.execute());
	}
}
