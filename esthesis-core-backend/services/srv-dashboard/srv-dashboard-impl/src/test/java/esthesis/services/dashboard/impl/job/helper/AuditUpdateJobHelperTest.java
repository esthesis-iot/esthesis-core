package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.audit.resource.AuditSystemResource;
import esthesis.service.common.paging.Page;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateAudit;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@QuarkusTest
class AuditUpdateJobHelperTest {

	@Inject
	AuditUpdateJobHelper auditUpdateJobHelper;

	@Inject
	TestHelper testHelper;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	AuditSystemResource auditSystemResource;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SecuritySystemResource securitySystemResource;


	@Test
	void refreshWithSecurityError() {
		// Mock the security as not permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(false);

		// Arrange a dashboard and an Audit item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-audit-dashboard");
		DashboardItemDTO item =
			testHelper.makeDashboardItem("test-audit-item", 0, AppConstants.Dashboard.Type.AUDIT)
				.setConfiguration("{\"entries\": 1}");

		// Assert that the security check fails.
		assertTrue(auditUpdateJobHelper.refresh(dashboardEntity, item).isSecurityError());
	}

	@Test
	void refresh() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock the find audit response.
		Page<AuditEntity> auditEntityPage = new Page<>();
		auditEntityPage.setContent(List.of(new AuditEntity().setCreatedBy("test-user").setMessage("test-message")));
		when(auditSystemResource.find(anyInt())).thenReturn(auditEntityPage);

		// Arrange a dashboard and an AUDIT item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-audit-dashboard");
		DashboardItemDTO item =
			testHelper.makeDashboardItem("test-audit-item", 0, AppConstants.Dashboard.Type.AUDIT)
				.setConfiguration("{\"entries\": 1}");

		// Assert the audit item was updated correctly.
		DashboardUpdateAudit dashboardUpdateAudit = auditUpdateJobHelper.refresh(dashboardEntity, item);
		assertEquals("test-user", dashboardUpdateAudit.getAuditEntries().getFirst().getLeft());
		assertEquals("test-message", dashboardUpdateAudit.getAuditEntries().getFirst().getRight());

	}

	@Test
	void refreshWithError() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Arrange a dashboard and an Audit item without required configuration.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-audit-dashboard");
		DashboardItemDTO item =
			testHelper.makeDashboardItem("test-audit-item", 0, AppConstants.Dashboard.Type.AUDIT);

		// Assert that the refresh method results in an error.
		assertTrue(auditUpdateJobHelper.refresh(dashboardEntity, item).isError());
	}
}
