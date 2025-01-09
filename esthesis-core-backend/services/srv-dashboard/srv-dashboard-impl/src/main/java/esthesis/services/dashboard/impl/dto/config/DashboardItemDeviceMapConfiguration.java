package esthesis.services.dashboard.impl.dto.config;

import lombok.Data;

@Data
public class DashboardItemDeviceMapConfiguration {

	private String[] hardwareIds;
	private String[] tags;
	private int zoom;
	private String mapLat;
	private String mapLng;
	private int height;
}
