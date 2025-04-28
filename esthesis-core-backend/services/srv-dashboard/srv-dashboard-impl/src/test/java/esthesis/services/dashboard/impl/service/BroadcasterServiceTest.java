package esthesis.services.dashboard.impl.service;

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
import esthesis.util.kafka.notifications.common.AppMessage;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants;
import esthesis.util.redis.RedisUtils;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.server.jaxrs.SseEventSinkImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_GEO_LAT;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_GEO_LON;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action.REFRESHSUB;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action.UNSUB;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class BroadcasterServiceTest {

	@Inject
	BroadcasterService broadcasterService;

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

	// Mocked user ID.
	ObjectId userId = new ObjectId();

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
		broadcasterService.cleanup();

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
	}

	@Test
	void cleanup() {
		// Create a dashboard and register a subscription.
		DashboardEntity dashboard = dashboardService.saveNew(testHelper.makeDashboard("test dashboard"));
		broadcasterService.register(dashboard.getId().toHexString(), "subscriptionId-1", mock(SseEventSinkImpl.class));

		assertDoesNotThrow(() -> broadcasterService.cleanup());
	}

	@Test
	void checkStaleJobs() {
		// Create a dashboard and register a subscription.
		DashboardEntity dashboard = dashboardService.saveNew(testHelper.makeDashboard("test dashboard"));
		broadcasterService.register(dashboard.getId().toHexString(), "subscriptionId-2", mock(SseEventSinkImpl.class));

		assertDoesNotThrow(() -> broadcasterService.checkStaleJobs());
	}

	@Test
	void onMessage() {
		// Create a dashboard and register the subscriptions.
		DashboardEntity dashboard = dashboardService.saveNew(testHelper.makeDashboard("test dashboard"));
		broadcasterService.register(dashboard.getId().toHexString(), "subscriptionId-3", mock(SseEventSinkImpl.class));
		broadcasterService.register(dashboard.getId().toHexString(), "subscriptionId-4", mock(SseEventSinkImpl.class));

		// Mock Kafka Record for UNSUB.
		KafkaRecord<String, AppMessage> unsubMsg = mock(KafkaRecord.class);
		AppMessage unsubPayload = AppMessage.builder()
			.component(KafkaNotificationsConstants.Component.DASHBOARD)
			.action(UNSUB)
			.targetId("subscriptionId-3")
			.build();

		when(unsubMsg.getPayload()).thenReturn(unsubPayload);
		when(unsubMsg.getMetadata()).thenReturn(Metadata.empty());
		when(unsubMsg.ack()).thenReturn(CompletableFuture.completedFuture(null));

		// Mock Kafka Record for REFRESHSUB.
		KafkaRecord<String, AppMessage> refreshSubMsg = mock(KafkaRecord.class);
		AppMessage refreshSubPayload = AppMessage.builder()
			.component(KafkaNotificationsConstants.Component.DASHBOARD)
			.action(REFRESHSUB)
			.targetId("subscriptionId-4")
			.build();
		when(refreshSubMsg.getPayload()).thenReturn(refreshSubPayload);
		when(refreshSubMsg.getMetadata()).thenReturn(Metadata.empty());
		when(refreshSubMsg.ack()).thenReturn(CompletableFuture.completedFuture(null));

		// Execute the onMessage method for both messages.
		CompletionStage<Void> unsubResult = broadcasterService.onMessage(unsubMsg);
		CompletionStage<Void> refreshSubResult = broadcasterService.onMessage(refreshSubMsg);

		// Assert that the CompletableFuture completes successfully.
		assertDoesNotThrow(() -> unsubResult.toCompletableFuture().join());
		assertDoesNotThrow(() -> refreshSubResult.toCompletableFuture().join());

		// Verify ack called.
		verify(unsubMsg).ack();
		verify(refreshSubMsg).ack();

	}

	@Test
	void register() {
		// Create a dashboard and register the subscriptions.
		DashboardEntity dashboard = dashboardService.saveNew(testHelper.makeDashboard("test dashboard"));

		assertDoesNotThrow(() -> broadcasterService.register(dashboard.getId().toHexString(),
			"subscriptionId-5",
			mock(SseEventSinkImpl.class)));


	}
}
