package esthesis.service.common;

import esthesis.core.common.entity.BaseEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

/**
 * Base service class for all services, providing common functionality to services. Most methods of
 * this class are 'protected', to disallow using them directly from the API. This is to ensure that
 * the service methods are used instead, which can (should) be annotated with security annotations.
 *
 * @param <D> The entity type to manage.
 */
@Slf4j
public abstract class BaseService<D extends BaseEntity> {

	// The repository to use for the entity.
	@Inject
	@Getter
	@SuppressWarnings("CdiInjectionPointsInspection")
	PanacheMongoRepository<D> repository;

	/**
	 * Get all entities from the repository.
	 *
	 * @param sortField     The field to sort by.
	 * @param sortDirection The direction to sort in.
	 * @return A list of all entities.
	 */
	protected List<D> getAll(String sortField, Sort.Direction sortDirection) {
		return repository.listAll(Sort.by(sortField, sortDirection));
	}

	/**
	 * Get all entities from the repository.
	 *
	 * @return A list of all entities.
	 */
	protected List<D> getAll() {
		return repository.listAll();
	}

	/**
	 * Find a random entity from the repository.
	 *
	 * @return An optional entity.
	 */
	protected Optional<D> findRandom() {
		return repository.listAll().stream().findAny();
	}

	/**
	 * Find the first entity that matches the given column and value.
	 *
	 * @param column       The column to match.
	 * @param value        The value to match.
	 * @param partialMatch Whether to do a partial match.
	 * @return The first entity that matches the given column and value.
	 */
	@SuppressWarnings("java:S1192")
	protected D findFirstByColumn(String column, Object value, boolean partialMatch) {
		if (partialMatch) {
			return repository.find(column + " like ?1", value).firstResult();
		} else {
			return repository.find(column + " = ?1", value).firstResult();
		}
	}

	/**
	 * Find the first entity that matches the given column and value.
	 *
	 * @param column The column to match.
	 * @param value  The value to match.
	 * @return The first entity that matches the given column and value.
	 */
	protected D findFirstByColumn(String column, Object value) {
		return findFirstByColumn(column, value, false);
	}

	/**
	 * Find all entities that match the given column and value.
	 *
	 * @param column       The column to match.
	 * @param value        The value to match.
	 * @param partialMatch Whether to do a partial match.
	 * @return A list of all entities that match the given column and value.
	 */
	protected List<D> findByColumn(String column, Object value, boolean partialMatch) {
		return findByColumn(column, value, partialMatch, null, null);
	}

	/**
	 * Find all entities that match the given column and value, supporting ordering.
	 *
	 * @param column       The column to match.
	 * @param value        The value to match.
	 * @param partialMatch Whether to do a partial match.
	 * @param orderColumn  The column to order by.
	 * @param direction    The direction to order in.
	 * @return A list of all entities that match the given column and value.
	 */
	@SuppressWarnings("java:S1192")
	protected List<D> findByColumn(String column, Object value, boolean partialMatch,
		String orderColumn, Sort.Direction direction) {
		String query = column;

		if (partialMatch) {
			query += " like ?1";
		} else {
			query += " = ?1";
		}

		if (orderColumn != null && direction != null) {
			return repository.find(query, Sort.by(orderColumn, direction), value).list();
		} else {
			return repository.find(query, value).list();
		}
	}

	/**
	 * Find all entities that do not have a value set for the given column.
	 *
	 * @param column The column to match.
	 * @return A list of all entities without value set for the given column.
	 */
	protected List<D> findByColumnNull(String column) {
		return repository.find(column + " is null").list();
	}

	/**
	 * Find all entities that have a value set for the given column in a list of values.
	 *
	 * @param column       The column to match.
	 * @param values       The values to match.
	 * @param partialMatch Whether to do a partial match.
	 * @return A list of all entities that have a value set for the given column.
	 */
	protected List<D> findByColumnIn(String column, List<String> values, boolean partialMatch) {
		if (partialMatch) {
			return repository.find(column + " like ?1", String.join("|", values)).list();
		} else {
			return repository.find(column + " in ?1", values).list();
		}
	}

	/**
	 * Find all entities that have a value set for the given column.
	 *
	 * @param column The column to match.
	 * @param value  The value to match.
	 * @return A list of all entities that have a value set for the given column.
	 */
	protected List<D> findByColumn(String column, Object value) {
		return findByColumn(column, value, false);
	}

	/**
	 * Count the number of entities that match the given column and value.
	 *
	 * @param column The column to match.
	 * @param value  The value to match.
	 * @return The number of entities that match the given column and value.
	 */
	protected long countByColumn(String column, Object value) {
		return repository.count(column + " = ?1", value);
	}

	/**
	 * Counts all entities.
	 *
	 * @return The total number of entities.
	 */
	protected long countAll() {
		return repository.count();
	}

	/**
	 * Return all entities, supporting paging.
	 *
	 * @param pageable Representation of page, size, and sort search parameters.
	 * @return A page of entities that match the given column and value.
	 */
	protected Page<D> find(Pageable pageable) {
		return find(pageable, false);
	}

	/**
	 * Find all entities, supporting paging and partial matching.
	 *
	 * @param pageable     Representation of page, size, and sort search parameters.
	 * @param partialMatch Whether to do a partial match.
	 * @return A page of entities that match the given column and value.
	 */
	protected Page<D> find(Pageable pageable, boolean partialMatch) {
		// Create a wrapper for the results.
		Page<D> quarkusPage = new Page<>();

		// Execute the query to get count and results.
		if (pageable.hasQuery()) {
			quarkusPage.setTotalElements(
				repository.count(pageable.getQueryKeys(partialMatch), pageable.getQueryValues()));
			pageable.getPageObject().ifPresentOrElse(val ->
					quarkusPage.setContent(
						repository.find(pageable.getQueryKeys(partialMatch), pageable.getSortObject(),
							pageable.getQueryValues()).page(val).list())
				, () ->
					quarkusPage.setContent(
						repository.find(pageable.getQueryKeys(partialMatch), pageable.getSortObject(),
							pageable.getQueryValues()).list()));
		} else {
			pageable.getPageObject().ifPresentOrElse(val -> {
				quarkusPage.setTotalElements(repository.count());
				quarkusPage.setContent(repository.findAll(pageable.getSortObject()).page(val).list());
			}, () -> {
				quarkusPage.setTotalElements(repository.count());
				quarkusPage.setContent(repository.findAll(pageable.getSortObject()).list());
			});
		}

		// Set query metadata.
		if (pageable.getPageObject().isPresent()) {
			quarkusPage.setIndex(pageable.getPage());
			quarkusPage.setSize(pageable.getSize());
		} else {
			quarkusPage.setIndex(0);
			quarkusPage.setSize(quarkusPage.getContent().size());
		}

		return quarkusPage;
	}

	/**
	 * Save the given entity. If the entity has an ID, it will be updated, otherwise it will be
	 * created.
	 *
	 * @param entity The entity to save.
	 * @return The saved entity.
	 */
	@Transactional
	protected D save(D entity) {
		if (entity.getId() != null) {
			log.trace("Updating entity with ID '{}'.", entity.getId());
			repository.update(entity);
		} else {
			ObjectId id = new ObjectId();
			log.trace("Creating new entity with ID '{}'.", entity.getId());
			entity.setId(id);
			repository.persist(entity);
		}

		return repository.findById(entity.getId());
	}

	/**
	 * Find an entity by its ID.
	 *
	 * @param id The ID of the entity to find.
	 * @return The entity with the given ID.
	 */
	protected D findById(String id) {
		return repository.findById(new ObjectId(id));
	}

	/**
	 * Find entities by their IDs.
	 *
	 * @param ids The IDs of the entities to find.
	 * @return The entities with the given IDs.
	 */
	protected List<D> findByIds(List<String> ids) {
		return repository.list("_id in ?1", ids.stream().map(ObjectId::new).toList());
	}

	/**
	 * Find an entity by its ID, returning an optional.
	 *
	 * @param id The ID of the entity to find.
	 * @return The entity with the given ID.
	 */
	protected Optional<D> findByIdOptional(String id) {
		return repository.findByIdOptional(new ObjectId(id));
	}

	/**
	 * Find an entity by its ID.
	 *
	 * @param id The ID of the entity to find.
	 * @return The entity with the given ID.
	 */
	protected D findById(ObjectId id) {
		return repository.findById(id);
	}

	/**
	 * Delete an entity by its ID.
	 *
	 * @param id The ID of the entity to delete.
	 * @return Whether the entity was deleted.
	 */
	@Transactional
	protected boolean deleteById(String id) {
		return repository.deleteById(new ObjectId(id));
	}

	/**
	 * Delete an entity by its ID.
	 *
	 * @param entity The entity to delete.
	 */
	@Transactional
	protected void delete(D entity) {
		repository.delete(entity);
	}

	/**
	 * Delete all entities.
	 *
	 * @return The number of entities deleted.
	 */
	@Transactional
	protected long deleteAll() {
		return repository.deleteAll();
	}

	/**
	 * Delete all entities that match the given column and value.
	 *
	 * @param columnName The column name to match.
	 * @param value      The value to match. Be careful to set the object type expected by the
	 *                   column.
	 * @return The number of entities deleted.
	 */
	@Transactional
	protected long deleteByColumn(String columnName, Object value) {
		// Sanity check when seemingly trying to delete an ID column without providing an ObjectId.
		if (StringUtils.endsWithIgnoreCase(columnName, "id") && !(value instanceof ObjectId)) {
			log.warn("Trying to delete by ID column without providing an ObjectId. "
					+ "Column: '{}', Value: '{}'.",
				columnName, value);
		}

		return repository.delete(columnName + " = ?1", value);
	}

}
