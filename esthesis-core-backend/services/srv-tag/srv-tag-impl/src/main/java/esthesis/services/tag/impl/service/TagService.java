package esthesis.services.tag.impl.service;

import static esthesis.core.common.AppConstants.ROLE_SYSTEM;
import static esthesis.core.common.AppConstants.Security.Category.TAGS;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.tag.entity.TagEntity;
import esthesis.services.tag.impl.repository.TagRepository;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import esthesis.util.kafka.notifications.outgoing.KafkaNotification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing tags.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class TagService extends BaseService<TagEntity> {

	private final TagRepository tagRepository;

	/**
	 * Save handler for tag entity.
	 *
	 * @param tagEntity Tag entity to save.
	 * @return Saved tag entity.
	 */
	private TagEntity saveHandler(TagEntity tagEntity) {
		return super.save(tagEntity);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = TAGS, operation = READ)
	public Page<TagEntity> find(Pageable pageable) {
		return super.find(pageable);
	}

	/**
	 * Creates a new tag.
	 *
	 * @param tagEntity the tag to create.
	 * @return the created tag.
	 */
	@Transactional
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = TAGS, operation = CREATE)
	public TagEntity saveNew(TagEntity tagEntity) {
		return saveHandler(tagEntity);
	}

	/**
	 * Updates an existing tag.
	 *
	 * @param tagEntity the tag to update.
	 * @return the updated tag.
	 */
	@Transactional
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = TAGS, operation = WRITE)
	public TagEntity saveUpdate(TagEntity tagEntity) {
		return saveHandler(tagEntity);
	}

	@Override
	@Transactional
	@ErnPermission(category = TAGS, operation = DELETE)
	@KafkaNotification(component = Component.TAG, subject = Subject.TAG, action = Action.DELETE,
		idParamOrder = 0)
	public boolean deleteById(String id) {
		return super.deleteById(id);
	}

	/**
	 * Finds tags by name.
	 *
	 * @param name the name of the tag to find.
	 * @return the list of tags found.
	 */
	@ErnPermission(category = TAGS, operation = READ)
	public List<TagEntity> findByName(String name) {
		return findByName(Collections.singletonList(name));
	}

	/**
	 * Finds tags by names.
	 *
	 * @param names the names of the tags to find.
	 * @return the list of tags found.
	 */
	@ErnPermission(category = TAGS, operation = READ)
	public List<TagEntity> findByName(List<String> names) {
		return tagRepository.findByName(names);
	}


	@Override
	@ErnPermission(category = TAGS, operation = READ)
	public List<TagEntity> getAll() {
		return super.getAll();
	}

	@Override
	@ErnPermission(category = TAGS, operation = READ)
	public TagEntity findById(String id) {
		return super.findById(id);
	}

	@Override
	@ErnPermission(category = TAGS, operation = READ)
	public List<TagEntity> findByColumn(String column, Object value) {
		return super.findByColumn(column, value);
	}

	@Override
	@ErnPermission(category = TAGS, operation = READ)
	public List<TagEntity> findByColumnIn(String column, List<String> values) {
		return super.findByColumnIn(column, values);
	}

	@Override
	@ErnPermission(category = TAGS, operation = READ)
	public TagEntity findFirstByColumn(String column, Object value) {
		return super.findFirstByColumn(column, value);
	}

	@Override
	@ErnPermission(category = TAGS, operation = READ)
	public List<TagEntity> findByIds(List<String> ids) {
		return super.findByIds(ids);
	}
}
