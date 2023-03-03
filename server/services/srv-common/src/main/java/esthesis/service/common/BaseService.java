package esthesis.service.common;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import esthesis.common.entity.BaseEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
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

  public D save(D entity) {
    if (entity.getId() != null) {
      repository
          .mongoCollection()
          .findOneAndUpdate(new Document("_id", entity.getId()),
              new Document("$set", entity),
              new FindOneAndUpdateOptions()
                  .returnDocument(ReturnDocument.AFTER));
    } else {
      ObjectId id = new ObjectId();
      entity.setId(id);
      repository.persist(entity);
    }

    return repository.findById(entity.getId());
  }

  public D findById(String id) {
    return repository.findById(new ObjectId(id));
  }

  public boolean deleteById(String deviceId) {
    return repository.deleteById(new ObjectId(deviceId));
  }

  public long deleteAll() {
    return repository.deleteAll();
  }

  public long deleteByColumn(String columnName, Object value) {
    return repository.delete(columnName + " = ?1", value);
  }

}
