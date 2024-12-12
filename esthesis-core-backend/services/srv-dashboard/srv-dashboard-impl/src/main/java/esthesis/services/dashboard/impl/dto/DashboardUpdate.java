package esthesis.services.dashboard.impl.dto;

import java.util.List;
import lombok.Data;

@Data
public class DashboardUpdate {

	private List<DashboardUpdateAudit> audit;
}
