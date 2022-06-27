package esthesis.service;

import esthesis.dto.BaseDTO;
import esthesis.mapper.BaseMapper;
import esthesis.resource.Pageable;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.inject.Inject;
import org.bson.types.ObjectId;

public abstract class BaseService<D extends BaseDTO> {

  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  private PanacheMongoRepository<D> repository;

  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  private BaseMapper<D> mapper;

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
      dto.setId(new ObjectId());
      repository.persist(dto);
    }

    return repository.findById(dto.getId());
  }
}
