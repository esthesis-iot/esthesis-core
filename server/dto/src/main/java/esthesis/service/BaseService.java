package esthesis.service;

import esthesis.dto.BaseDTO;
import esthesis.mapper.BaseMapper;
import esthesis.repository.BaseRepository;
import java.util.List;
import javax.inject.Inject;

public abstract class BaseService<D extends BaseDTO> {

  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  private BaseRepository<D> repository;

  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  private BaseMapper<D> mapper;

  public List<D> getAll() {
    return repository.findAll().list();
  }

  public D save(D dto) {
    if (dto.getId() != null) {
      repository.update(mapper.map(dto, repository.findById(dto.getId())));
    } else {
      repository.persist(dto);
    }

    return repository.findById(dto.getId());
  }
}
