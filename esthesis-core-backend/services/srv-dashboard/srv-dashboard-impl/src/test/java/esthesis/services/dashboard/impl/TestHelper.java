package esthesis.services.dashboard.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.util.EsthesisCommonConstants.Device;
import esthesis.core.common.AppConstants.Dashboard;
import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.dto.DevicesLastSeenStatsDTO;
import esthesis.service.device.dto.DevicesTotalsStatsDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.security.entity.UserEntity;
import esthesis.services.dashboard.impl.dto.config.DashboardItemAuditConfiguration;
import esthesis.services.dashboard.impl.dto.config.DashboardItemCampaignsConfiguration;
import esthesis.services.dashboard.impl.dto.config.DashboardItemChartConfiguration;
import esthesis.services.dashboard.impl.dto.config.DashboardItemDeviceMapConfiguration;
import esthesis.services.dashboard.impl.dto.config.DashboardItemDevicesLatestConfiguration;
import esthesis.services.dashboard.impl.dto.config.DashboardItemDiffConfiguration;
import esthesis.services.dashboard.impl.dto.config.DashboardItemImageConfiguration;
import esthesis.services.dashboard.impl.dto.config.DashboardItemNotesConfiguration;
import esthesis.services.dashboard.impl.dto.config.DashboardItemSensorConfiguration;
import esthesis.services.dashboard.impl.dto.config.DashboardItemSensorIconConfiguration;
import esthesis.services.dashboard.impl.dto.config.DashboardItemTitleConfiguration;
import esthesis.services.dashboard.impl.repository.DashboardRepository;
import esthesis.util.kafka.notifications.common.AppMessage;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class TestHelper {

	@Inject
	DashboardRepository dashboardRepository;

	ObjectMapper objectMapper = new ObjectMapper();

	public void clearDatabase() {
		dashboardRepository.deleteAll();
	}

	public DashboardEntity makeDashboard(String name) {
		DashboardEntity dashboard = new DashboardEntity();
		dashboard.setName(name);
		dashboard.setHome(true);
		dashboard.setOwnerId(new ObjectId());
		dashboard.setShared(false);
		dashboard.setDisplayLastUpdate(true);
		dashboard.setUpdateInterval(10);
		dashboard.setDescription("test description");
		// Creates one dashboard item for each dashboard type.
		int index = 0;
		List<DashboardItemDTO> items = new ArrayList<>();
		for (Dashboard.Type type : Dashboard.Type.values()) {
			items.add(makeDashboardItem(type.name(), index++, type));
		}

		dashboard.setItems(items);
		return dashboard;
	}

	public DashboardItemDTO makeDashboardItem(String title, Integer index, Dashboard.Type type) {
		DashboardItemDTO dashboardItem = new DashboardItemDTO();
		dashboardItem.setId(new ObjectId().toHexString());
		dashboardItem.setColumns(1);
		dashboardItem.setConfiguration(makeConfigurationString(type));
		dashboardItem.setEnabled(true);
		dashboardItem.setSubtitle("test subtitle");
		dashboardItem.setIndex(index);
		dashboardItem.setTitle(title);
		dashboardItem.setType(type);
		return dashboardItem;
	}

	public DashboardItemAuditConfiguration makeConfigurationAudit(int entries) {
		DashboardItemAuditConfiguration config = new DashboardItemAuditConfiguration();
		config.setEntries(entries);
		return config;
	}

	public DashboardItemCampaignsConfiguration makeConfigurationCampaign(int entries) {
		DashboardItemCampaignsConfiguration config = new DashboardItemCampaignsConfiguration();
		config.setEntries(entries);
		return config;
	}

	public DashboardItemChartConfiguration makeConfigurationChart(String hardwareIds, String tags, String measurements) {
		DashboardItemChartConfiguration config = new DashboardItemChartConfiguration();
		config.setHardwareIds(Arrays.stream(hardwareIds.split(",")).toList());
		config.setTags(Arrays.stream(tags.split(",")).toList());
		config.setMeasurements(Arrays.stream(measurements.split(",")).toList());
		return config;
	}

	public DashboardItemDeviceMapConfiguration makeConfigurationDeviceMap(String hardwareIds,
																																				String tags,
																																				String lat,
																																				String lng,
																																				int zoom,
																																				int height) {
		DashboardItemDeviceMapConfiguration config = new DashboardItemDeviceMapConfiguration();
		config.setHardwareIds(hardwareIds.split(","));
		config.setTags(tags.split(","));
		config.setZoom(zoom);
		config.setMapLng(lng);
		config.setMapLat(lat);
		config.setHeight(height);
		return config;
	}

	public DashboardItemDevicesLatestConfiguration makeConfigurationDevicesLatest(
		int entries) {
		DashboardItemDevicesLatestConfiguration config = new DashboardItemDevicesLatestConfiguration();
		config.setEntries(entries);
		return config;
	}

	public DashboardItemDiffConfiguration makeConfigurationDiff(String hardwareId,
																															String measurement,
																															String unit, String icon,
																															long items) {
		DashboardItemDiffConfiguration config = new DashboardItemDiffConfiguration();
		config.setHardwareId(hardwareId);
		config.setMeasurement(measurement);
		config.setUnit(unit);
		config.setIcon(icon);
		config.setItems(items);
		return config;
	}

	public DashboardItemImageConfiguration makeConfigurationImage(String imageUrl,
																																int height,
																																int refresh) {
		DashboardItemImageConfiguration config = new DashboardItemImageConfiguration();
		config.setImageUrl(imageUrl);
		config.setHeight(height);
		config.setRefresh(refresh);
		return config;
	}

	public DashboardItemNotesConfiguration makeConfigurationNotes(String notes) {
		DashboardItemNotesConfiguration config = new DashboardItemNotesConfiguration();
		config.setNotes(notes);
		return config;
	}

	public DashboardItemSensorConfiguration makeConfigurationSensor(String hardwareId,
																																	String measurement,
																																	String unit,
																																	String icon,
																																	int precision) {
		DashboardItemSensorConfiguration config = new DashboardItemSensorConfiguration();
		config.setHardwareId(hardwareId);
		config.setMeasurement(measurement);
		config.setUnit(unit);
		config.setIcon(icon);
		config.setPrecision(precision);
		config.setSparkline(true);
		config.setSparklinePoints(5);
		config.setThreshold(false);
		return config;
	}

	public DashboardItemSensorIconConfiguration makeConfigurationSensorIcon(String hardwareId,
																																					String measurement,
																																					String unit,
																																					int precision) {
		DashboardItemSensorIconConfiguration config = new DashboardItemSensorIconConfiguration();
		config.setHardwareId(hardwareId);
		config.setMeasurement(measurement);
		config.setUnit(unit);
		config.setPrecision(precision);
		return config;
	}

	public DashboardItemTitleConfiguration makeConfigurationTitle(String title) {
		DashboardItemTitleConfiguration config = new DashboardItemTitleConfiguration();
		config.setTitle(title);
		return config;
	}

	public Principal makePrincipal(String username) {
		return () -> username;
	}

	public UserEntity makeUser(String username, ObjectId userId) {
		UserEntity user = new UserEntity();
		user.setUsername(username);
		user.setPolicies(List.of("test-policy"));
		user.setEmail("test-email@eurodyn.com");
		user.setDescription("test-description");
		user.setFirstName("Test");
		user.setLastName("User");
		user.setGroups(List.of("test-group"));
		user.setId(userId);
		return user;
	}

	public DeviceEntity makeCoreDevice(String hardwareId) {
		DeviceEntity device = new DeviceEntity();
		device.setHardwareId(hardwareId);
		device.setId(new ObjectId());
		device.setType(Device.Type.CORE);
		device.setRegisteredOn(Instant.now().minus(1, ChronoUnit.DAYS));
		return device;
	}

	public AboutGeneralDTO makeAboutGeneral() {
		AboutGeneralDTO generalInfo = new AboutGeneralDTO();
		generalInfo.setGitBuildTime("2025-04-09T00:00:00Z");
		generalInfo.setGitCommitId("test-id");
		generalInfo.setGitVersion("test-version");
		generalInfo.setGitCommitIdAbbrev("test-abbrev");
		return generalInfo;
	}

	public DevicesLastSeenStatsDTO makeDeviceLastSeenStats() {
		DevicesLastSeenStatsDTO stats = new DevicesLastSeenStatsDTO();
		stats.setSeenLastDay(1);
		stats.setSeenLastWeek(2);
		stats.setSeenLastMonth(3);
		stats.setSeenLastHour(10);
		stats.setSeenLastMinute(15);
		return stats;
	}

	public DevicesTotalsStatsDTO makeDeviceTotalStats() {
		DevicesTotalsStatsDTO stats = new DevicesTotalsStatsDTO();
		stats.setDisabled(0L);
		stats.setRegistered(1L);
		stats.setPreregistered(1L);
		stats.setTotal(10L);
		return stats;
	}

	/**
	 * Creates a valid  configuration string  with test values for the given dashboard type.
	 * @param type the type of the dashboard item.
	 * @return the configuration string.
	 */
	@SneakyThrows
	private String makeConfigurationString(Dashboard.Type type) {
		return switch (type) {
			case AUDIT -> objectMapper.writeValueAsString(makeConfigurationAudit(1));
			case CAMPAIGNS -> objectMapper.writeValueAsString(makeConfigurationCampaign(1));
			case CHART ->
				objectMapper.writeValueAsString(makeConfigurationChart("test-hardware", "test-tag", "test-measurement"));
			case DEVICE_MAP ->
				objectMapper.writeValueAsString(makeConfigurationDeviceMap("test-hardware", "test-tag", "37.969870", "23.718280", 10, 100));
			case DEVICES_LATEST -> objectMapper.writeValueAsString(makeConfigurationDevicesLatest(1));
			case DIFF ->
				objectMapper.writeValueAsString(makeConfigurationDiff("test-hardware", "test-measurement", "unit", "icon.png", 1));
			case IMAGE -> objectMapper.writeValueAsString(makeConfigurationImage("image.png", 100, 1));
			case SENSOR ->
				objectMapper.writeValueAsString(makeConfigurationSensor("test-hardware", "test-measurement", "unit", "icon.png", 1));
			case SENSOR_ICON ->
				objectMapper.writeValueAsString(makeConfigurationSensorIcon("test-hardware", "test-measurement", "unit", 1));
			case TITLE -> objectMapper.writeValueAsString(makeConfigurationTitle("test-title"));
			case NOTES -> objectMapper.writeValueAsString(makeConfigurationNotes("test-note"));
			default -> "test configuration";
		};
	}

	public AppMessage makeAppMessage(KafkaNotificationsConstants.Component component, KafkaNotificationsConstants.Action action) {
		return AppMessage.builder()
			.component(component)
			.subject(KafkaNotificationsConstants.Subject.DASHBOARD)
			.action(action)
			.msgId("test-msg-id")
			.targetId("test-target-id")
			.comment("test-comment")
			.broadcast(false)
			.build();
	}
}
