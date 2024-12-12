package esthesis.services.dashboard.impl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardUpdateAudit {

	private String username;
	private String message;
}
