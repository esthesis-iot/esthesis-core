package esthesis.services.dashboard.impl.service;

import static esthesis.core.common.AppConstants.ROLE_SYSTEM;
import static esthesis.core.common.AppConstants.Security.Category.DASHBOARD;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.audit.resource.AuditSystemResource;
import esthesis.service.common.BaseService;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.security.entity.UserEntity;
import esthesis.service.security.resource.SecurityResource;
import io.quarkus.panache.common.Sort.Direction;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DashboardService extends BaseService<DashboardEntity> {

	private final SecurityIdentity securityIdentity;
	@Inject
	@RestClient
	SecurityResource securityResource;

	@Inject
	@RestClient
	AuditSystemResource auditSystemResource;

	private UserEntity findCurrentUser() {
		String username = securityIdentity.getPrincipal().getName();
		UserEntity user = securityResource.findUserByUsername(username);
		if (user == null) {
			throw new QDoesNotExistException("User '{}' does not exist.", username);
		}

		return user;
	}

	private DashboardEntity saveHandler(DashboardEntity dashboardEntity) {
		UserEntity user = findCurrentUser();
		dashboardEntity.setOwnerId(user.getId());

		// If no other dashboards exist for this user, set this one as the home dashboard. Otherwise,
		// if this dashboard is set as the home dashboard, unset the home dashboard for all other
		// dashboards.
		if (findAllForCurrentUser().isEmpty()) {
			dashboardEntity.setHome(true);
		} else if (dashboardEntity.isHome()) {
			List<DashboardEntity> dashboards = findAllForCurrentUser();
			for (DashboardEntity dashboard : dashboards) {
				if (dashboard.isHome()) {
					dashboard.setHome(false);
					super.save(dashboard);
				}
			}
		}

		return super.save(dashboardEntity);
	}

	@Transactional
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = DASHBOARD, operation = CREATE)
	public DashboardEntity saveNew(DashboardEntity dashboardEntity) {
		return saveHandler(dashboardEntity);
	}

	@Transactional
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = DASHBOARD, operation = WRITE)
	public DashboardEntity saveUpdate(DashboardEntity dashboardEntity) {
		return saveHandler(dashboardEntity);
	}

	@Override
	@Transactional
	@ErnPermission(category = DASHBOARD, operation = DELETE)
	public boolean deleteById(String id) {
		log.debug("Deleting dashboard with id '{}'.", id);

		// If this dashboard is the home dashboard, set the home dashboard to the next dashboard in the
		// list of dashboards.
		DashboardEntity dashboardEntity = findById(id);
		if (dashboardEntity.isHome()) {
			List<DashboardEntity> dashboards = findAllForCurrentUser();
			if (dashboards.size() > 1) {
				dashboards.getFirst().setHome(true);
				super.save(dashboards.getFirst());
			}
		}

		return super.deleteById(id);
	}

	@Override
	@ErnPermission(category = DASHBOARD, operation = READ)
	public Optional<DashboardEntity> findByIdOptional(String id) {
		return super.findByIdOptional(id);
	}

	@ErnPermission(category = DASHBOARD, operation = READ)
	public List<DashboardEntity> findAllForCurrentUser() {
		UserEntity user = findCurrentUser();
		return super.findByColumn("ownerId", user.getId(), false, "name", Direction.Ascending);
	}

	@ErnPermission(category = DASHBOARD, operation = READ)
	public List<DashboardEntity> findShared() {
		return super.findByColumn("shared", true, false, "name", Direction.Ascending);
	}

}
