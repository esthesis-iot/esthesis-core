package esthesis.services.dashboard.impl;

import esthesis.common.util.EsthesisCommonConstants.Device;
import esthesis.core.common.AppConstants.Dashboard;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.security.entity.UserEntity;
import esthesis.services.dashboard.impl.repository.DashboardRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TestHelper {

	@Inject
	DashboardRepository dashboardRepository;

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
		dashboardItem.setConfiguration("test configuration");
		dashboardItem.setEnabled(true);
		dashboardItem.setSubtitle("test subtitle");
		dashboardItem.setIndex(index);
		dashboardItem.setTitle(title);
		dashboardItem.setType(type);
		return dashboardItem;
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
}
