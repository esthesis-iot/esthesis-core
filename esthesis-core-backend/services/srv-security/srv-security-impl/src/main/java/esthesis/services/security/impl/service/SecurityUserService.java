package esthesis.services.security.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.security.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@Transactional
@ApplicationScoped
public class SecurityUserService extends BaseService<UserEntity> {

	@ConfigProperty(name = "esthesis.security.admin.username")
	String esthesisAdminUsername;

	@ConfigProperty(name = "esthesis.security.defaultAdminGroupId")
	String defaultAdminGroupId;

	public UserEntity findByUsername(String username) {
		return findFirstByColumn("username", username);
	}

	//TODO review this method, is this logic still needed? Could we inject this logic in liquibase
	//scripts or similar?
	/**
	 * If User collection is empty, create a default admin user.
	 */
	public void createDefaultAdmin() {
		if (getRepository().count() == 0) {
			log.info("No administrators found. Creating default admin user with username '{}'.",
				esthesisAdminUsername);
			UserEntity user = new UserEntity();
			user.setUsername(esthesisAdminUsername);
			user.setDescription("Default system-created admin account.");
			user.setFirstName("esthesis");
			user.setLastName("admin");
			user.setGroups(List.of(defaultAdminGroupId));
			save(user);
			log.info("Default admin user created.");
		}
	}
}
