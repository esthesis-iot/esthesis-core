package esthesis.services.security.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.security.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SecurityUserService extends BaseService<UserEntity> {

	public UserEntity findByUsername(String username) {
		return findFirstByColumn("username", username);
	}
}
