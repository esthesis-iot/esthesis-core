package esthesis.service.common;

import esthesis.common.entity.BaseEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

@Slf4j
public abstract class BaseService<D extends BaseEntity> {

	@Inject
	@SuppressWarnings("CdiInjectionPointsInspection")
	PanacheMongoRepository<D> repository;

	public PanacheMongoRepository<D> getRepository() {
		return repository;
	}

	public List<D> getAll(String sortField, Sort.Direction sortDirection) {
		return repository.listAll(Sort.by(sortField, sortDirection));
	}

	public List<D> getAll() {
		return repository.listAll();
	}

	public Optional<D> findRandom() {
		return repository.listAll().stream().findAny();
	}

	@SuppressWarnings("java:S1192")
	public D findFirstByColumn(String column, Object value, boolean partialMatch) {
		if (partialMatch) {
			return repository.find(column + " like ?1", value).firstResult();
		} else {
			return repository.find(column + " = ?1", value).firstResult();
		}
	}

	public D findFirstByColumn(String column, Object value) {
		return findFirstByColumn(column, value, false);
	}

	@SuppressWarnings("java:S1192")
	public List<D> findByColumn(String column, Object value, boolean partialMatch) {
		if (partialMatch) {
			return repository.find(column + " like ?1", value).list();
		} else {
			return repository.find(column + " = ?1", value).list();
		}
	}

	public List<D> findByColumnNull(String column) {
		return repository.find(column + " is null").list();
	}

	public List<D> findByColumnIn(String column, List<String> values, boolean partialMatch) {
		if (partialMatch) {
			return repository.find(column + " like ?1", String.join("|", values)).list();
		} else {
			return repository.find(column + " in ?1", values).list();
		}
	}

	public List<D> findByColumn(String column, Object value) {
		return findByColumn(column, value, false);
	}

	public long countByColumn(String column, Object value) {
		return repository.count(column + " = ?1", value);
	}

	public Page<D> find(Pageable pageable) {
		return find(pageable, false);
	}

	public Page<D> find(Pageable pageable, boolean partialMatch) {
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
			quarkusPage.setPage(pageable.getPage());
			quarkusPage.setSize(pageable.getSize());
		} else {
			quarkusPage.setPage(0);
			quarkusPage.setSize(quarkusPage.getContent().size());
		}

		return quarkusPage;
	}

	@Transactional
	public D save(D entity) {
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

	public D findById(String id) {
		return repository.findById(new ObjectId(id));
	}

	@Transactional
	public boolean deleteById(String deviceId) {
		return repository.deleteById(new ObjectId(deviceId));
	}

	@Transactional
	public void delete(D entity) {
		repository.delete(entity);
	}

	@Transactional
	public long deleteAll() {
		return repository.deleteAll();
	}

	/**
	 * Delete all entities that match the given column and value.
	 * @param columnName The column name to match.
	 * @param value The value to match. Be careful to set the object type expected by the column.
	 * @return The number of entities deleted.
	 */
	@Transactional
	public long deleteByColumn(String columnName, Object value) {
		// Sanity check when seemingly trying to delete an ID column without providing an ObjectId.
		if (StringUtils.endsWithIgnoreCase(columnName, "id") && !(value instanceof ObjectId)) {
			log.warn("Trying to delete by ID column without providing an ObjectId. "
					+ "Column: '{}', Value: '{}'.",
				columnName, value);
		}

		return repository.delete(columnName + " = ?1", value);
	}

}
