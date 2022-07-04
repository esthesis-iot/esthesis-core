package esthesis.common.service;

import esthesis.common.dto.BaseDTO;
import esthesis.common.service.rest.Page;
import esthesis.common.service.rest.Pageable;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.inject.Inject;
import org.bson.types.ObjectId;

public abstract class BaseService<D extends BaseDTO> {

  @Inject
  @SuppressWarnings("CdiInjectionPointsInspection")
  PanacheMongoRepository<D> repository;

  public D findByColumn(String column, String value,
      boolean partialMatch) {
    if (partialMatch) {
      return repository.find(column + " like ?1", value).firstResult();
    } else {
      return repository.find(column + " = ?1", value).firstResult();
    }
  }

  public D findByColumn(String column, String value) {
    return findByColumn(column, value, false);
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
          repository.find(pageable.getQueryKeys(partialMatch),
              pageable.getQueryValues()).list().size());
      if (pageable.getPageObject().isPresent()) {
        quarkusPage.setContent(
            repository.find(pageable.getQueryKeys(partialMatch),
                    pageable.getSortObject(),
                    pageable.getQueryValues()).page(pageable.getPageObject().get())
                .list());
      } else {
        quarkusPage.setContent(
            repository.find(pageable.getQueryKeys(partialMatch),
                pageable.getSortObject(), pageable.getQueryValues()).list());
      }
    } else {
      if (pageable.getPageObject().isPresent()) {
        quarkusPage.setTotalElements(repository.count());
        quarkusPage.setContent(
            repository.findAll(pageable.getSortObject())
                .page(pageable.getPageObject().get()).list());
      } else {
        quarkusPage.setTotalElements(repository.count());
        quarkusPage.setContent(
            repository.findAll(pageable.getSortObject()).list());
      }

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

  public D findById(ObjectId id) {
    return repository.findById(id);
  }

  public D save(D dto) {
    if (dto.getId() != null) {
      repository.update(dto);
    } else {
      ObjectId id = new ObjectId();
      dto.setId(id);
      repository.persist(dto);
    }

    return repository.findById(dto.getId());
  }

  public void deleteById(ObjectId id) {
    repository.deleteById(id);
  }
}
