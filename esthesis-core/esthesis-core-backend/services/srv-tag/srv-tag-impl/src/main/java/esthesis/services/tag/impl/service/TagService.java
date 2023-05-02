package esthesis.services.tag.impl.service;

import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.common.exception.QSecurityException;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.common.validation.CVExceptionContainer;
import esthesis.service.security.resource.SecurityResource;
import esthesis.service.tag.entity.TagEntity;
import esthesis.services.tag.impl.repository.TagRepository;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import esthesis.util.kafka.notifications.outgoing.KafkaNotification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class TagService extends BaseService<TagEntity> {

	@Inject
	JsonWebToken jwt;

	@Inject
	TagRepository tagRepository;

	@Inject
	@RestClient
	SecurityResource securityResource;

	@Override
	public Page<TagEntity> find(Pageable pageable) {
		log.debug("Finding all tags with '{}'.", pageable);

		if (!securityResource.isPermitted(Category.TAG, Operation.READ)) {
			throw new QSecurityException("You are not allowed to view tags.");
		} else {
			return super.find(pageable);
		}
	}

	@Override
	public Page<TagEntity> find(Pageable pageable, boolean partialMatch) {
		log.debug("Finding all tags with partial match with '{}'.", pageable);
		if (!securityResource.isPermitted(Category.TAG, Operation.READ)) {
			throw new QSecurityException("You are not allowed to view tags.");
		} else {
			return super.find(pageable, partialMatch);
		}
	}

	@Override
	public TagEntity save(TagEntity tagEntity) {
		log.debug("Saving tag '{}'.", tagEntity);

		// Security check.
		if (tagEntity.getId() == null && !securityResource.isPermitted(Category.TAG,
			Operation.CREATE)) {
			throw new QSecurityException("You are not allowed to create tags.");
		} else if (!securityResource.isPermitted(Category.TAG, Operation.WRITE)) {
			throw new QSecurityException("You are not allowed to update tags.");
		}

		// Ensure no other tag has the same name.
		TagEntity existingTagEntity = findFirstByColumn("name", tagEntity.getName());
		if (existingTagEntity != null && (tagEntity.getId() == null || !existingTagEntity.getId()
			.equals(tagEntity.getId()))) {
			new CVExceptionContainer<TagEntity>().addViolation("name",
				"A tag with name '{}' already exists.", tagEntity.getName()).throwCVE();
		}

		return super.save(tagEntity);
	}

	@Override
	@KafkaNotification(component = Component.TAG, subject = Subject.TAG, action = Action.DELETE,
		idParamOrder = 0, payload = "Tag ID")
	public boolean deleteById(String id) {
		log.debug("Deleting tag with id '{}'.", id);
		if (!securityResource.isPermitted(Category.TAG, Operation.DELETE, id)) {
			throw new QSecurityException("You are not allowed to delete this tags.");
		} else {
			return super.deleteById(id);
		}
	}

	public List<TagEntity> findByName(String name, boolean partialMatch) {
		if (!securityResource.isPermitted(Category.TAG, Operation.READ)) {
			throw new QSecurityException("You are not allowed to view tags.");
		} else {
			return findByName(Collections.singletonList(name), partialMatch);
		}
	}

	public List<TagEntity> findByName(List<String> names, boolean partialMatch) {
		if (!securityResource.isPermitted(Category.TAG, Operation.READ)) {
			throw new QSecurityException("You are not allowed to view tags.");
		} else {
			if (partialMatch) {
				return tagRepository.findByNamePartial(names);
			} else {
				return tagRepository.findByName(names);
			}
		}
	}
}
