package esthesis.common.service;

import esthesis.common.dto.BaseDTO;
import esthesis.common.service.mapper.BaseMapper;
import esthesis.common.service.rest.Page;
import esthesis.common.service.rest.Pageable;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import java.util.Optional;
import javax.inject.Inject;
import org.bson.types.ObjectId;

public abstract class BaseService<D extends BaseDTO> {

  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  PanacheMongoRepository<D> repository;

  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  BaseMapper<D> mapper;

  public Optional<D> findByColumn(String column, String value,
      boolean partialMatch) {
    if (partialMatch) {
      return repository.find(column + " like ?1", value).firstResultOptional();
    } else {
      return repository.find(column + " = ?1", value).firstResultOptional();
    }

  }

  public Optional<D> findByColumn(String column, String value) {
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
      quarkusPage.setContent(
          repository.find(pageable.getQueryKeys(partialMatch),
              pageable.getSortObject(),
              pageable.getQueryValues()).page(pageable.getPageObject()).list());
    } else {
      quarkusPage.setTotalElements(repository.count());
      quarkusPage.setContent(
          repository.findAll(pageable.getSortObject())
              .page(pageable.getPageObject()).list());
    }

    // Set query metadata.
    quarkusPage.setPage(pageable.getPage());
    quarkusPage.setSize(pageable.getSize());

    return quarkusPage;
  }

  public D findById(ObjectId id) {
    return repository.findById(id);
  }

  public D save(D dto) {
    if (dto.getId() != null) {
      repository.update(mapper.map(dto, repository.findById(dto.getId())));
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
