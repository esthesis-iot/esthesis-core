package esthesis.services.tag.impl.service;

import static esthesis.common.AppConstants.ROLE_SYSTEM;
import static esthesis.common.AppConstants.Security.Category.TAG;
import static esthesis.common.AppConstants.Security.Operation.CREATE;
import static esthesis.common.AppConstants.Security.Operation.DELETE;
import static esthesis.common.AppConstants.Security.Operation.READ;
import static esthesis.common.AppConstants.Security.Operation.WRITE;

import esthesis.common.AppConstants.Security.Operation;
import esthesis.common.exception.QSecurityException;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.security.resource.SecurityResource;
import esthesis.service.tag.entity.TagEntity;
import esthesis.services.tag.impl.repository.TagRepository;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import esthesis.util.kafka.notifications.outgoing.KafkaNotification;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@Transactional
@ApplicationScoped
public class TagService extends BaseService<TagEntity> {

	@Inject
	TagRepository tagRepository;

	@Inject
	@RestClient
	SecurityResource securityResource;

	@Inject
	SecurityIdentity securityIdentity;

	private TagEntity saveHandler(TagEntity tagEntity) {
		return super.save(tagEntity);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = TAG, operation = READ)
	public Page<TagEntity> find(Pageable pageable) {
		return super.find(pageable);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = TAG, operation = READ)
	public Page<TagEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = TAG, operation = CREATE)
	public TagEntity saveNew(TagEntity tagEntity) {
		return saveHandler(tagEntity);
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = TAG, operation = WRITE)
	public TagEntity saveUpdate(TagEntity tagEntity) {
		return saveHandler(tagEntity);
	}

	@Override
	@KafkaNotification(component = Component.TAG, subject = Subject.TAG, action = Action.DELETE,
		idParamOrder = 0)
	@ErnPermission(category = TAG, operation = DELETE)
	public boolean deleteById(String id) {
		log.debug("Deleting tag with id '{}'.", id);
		if (!securityResource.isPermitted(TAG, Operation.DELETE, id)) {
			throw new QSecurityException("You are not allowed to delete this tags.");
		} else {
			return super.deleteById(id);
		}
	}

	@ErnPermission(category = TAG, operation = READ)
	public List<TagEntity> findByName(String name, boolean partialMatch) {
		if (!securityResource.isPermitted(TAG, Operation.READ)) {
			throw new QSecurityException("You are not allowed to view tags.");
		} else {
			return findByName(Collections.singletonList(name), partialMatch);
		}
	}

	@ErnPermission(category = TAG, operation = READ)
	public List<TagEntity> findByName(List<String> names, boolean partialMatch) {
		if (!securityResource.isPermitted(TAG, Operation.READ)) {
			throw new QSecurityException("You are not allowed to view tags.");
		} else {
			if (partialMatch) {
				return tagRepository.findByNamePartial(names);
			} else {
				return tagRepository.findByName(names);
			}
		}
	}

	@Override
	@ErnPermission(category = TAG, operation = READ)
	public List<TagEntity> getAll() {
		return super.getAll();
	}

	@Override
	@ErnPermission(category = TAG, operation = READ)
	public TagEntity findById(String id) {
		return super.findById(id);
	}

	@Override
	@ErnPermission(category = TAG, operation = READ)
	public List<TagEntity> findByColumn(String column, Object value) {
		return super.findByColumn(column, value);
	}

	@Override
	@ErnPermission(category = TAG, operation = READ)
	public List<TagEntity> findByColumnIn(String column, List<String> values,
		boolean partialMatch) {
		return super.findByColumnIn(column, values, partialMatch);
	}

	@Override
	@ErnPermission(category = TAG, operation = READ)
	public TagEntity findFirstByColumn(String column, Object value, boolean partialMatch) {
		return super.findFirstByColumn(column, value, partialMatch);
	}
}
