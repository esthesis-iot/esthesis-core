package esthesis.service.common;

import esthesis.common.entity.BaseEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

@Slf4j
public abstract class BaseService<D extends BaseEntity> {

  @Inject
  @SuppressWarnings("CdiInjectionPointsInspection")
  PanacheMongoRepository<D> repository;

  protected PanacheMongoRepository<D> getRepository() {
    return repository;
  }

  protected List<D> getAll(String sortField, Sort.Direction sortDirection) {
    return repository.listAll(Sort.by(sortField, sortDirection));
  }

  protected List<D> getAll() {
    return repository.listAll();
  }

  @SuppressWarnings("java:S1192")
  protected D findFirstByColumn(String column, String value, boolean partialMatch) {
    if (partialMatch) {
      return repository.find(column + " like ?1", value).firstResult();
    } else {
      return repository.find(column + " = ?1", value).firstResult();
    }
  }

  protected D findFirstByColumn(String column, String value) {
    return findFirstByColumn(column, value, false);
  }

  @SuppressWarnings("java:S1192")
  protected List<D> findByColumn(String column, String value, boolean partialMatch) {
    if (partialMatch) {
      return repository.find(column + " like ?1", value).list();
    } else {
      return repository.find(column + " = ?1", value).list();
    }
  }

  protected List<D> findByColumnNull(String column) {
    return repository.find(column + " is null").list();
  }

  protected List<D> findByColumnIn(String column, List<String> values, boolean partialMatch) {
    if (partialMatch) {
      return repository.find(column + " like ?1", String.join("|", values)).list();
    } else {
      return repository.find(column + " in ?1", values).list();
    }
  }

  protected List<D> findByColumn(String column, String value) {
    return findByColumn(column, value, false);
  }

  protected List<D> findByColumn(String column, int value) {
    return repository.find(column + " = ?1", value).list();
  }

  protected long countByColumn(String column, String value) {
    return repository.count(column + " = ?1", value);
  }

  protected Page<D> find(Pageable pageable) {
    return find(pageable, false);
  }

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
      quarkusPage.setPage(pageable.getPage());
      quarkusPage.setSize(pageable.getSize());
    } else {
      quarkusPage.setPage(0);
      quarkusPage.setSize(quarkusPage.getContent().size());
    }

    return quarkusPage;
  }

  protected D findById(ObjectId id) {
    return repository.findById(id);
  }

  protected D findById(String id) {
    return repository.findById(new ObjectId(id));
  }

  protected D save(D dto) {
    if (dto.getId() != null) {
      repository.update(dto);
    } else {
      ObjectId id = new ObjectId();
      dto.setId(id);
      repository.persist(dto);
    }

    return repository.findById(dto.getId());
  }

  protected boolean deleteById(ObjectId id) {
    return repository.deleteById(id);
  }

  protected boolean deleteById(String id) {
    return repository.deleteById(new ObjectId(id));
  }

  protected long deleteAll() {
    return repository.deleteAll();
  }

  protected long deleteByColumn(String columnName, Object value) {
    return repository.delete(columnName + " = ?1", value);
  }
}
