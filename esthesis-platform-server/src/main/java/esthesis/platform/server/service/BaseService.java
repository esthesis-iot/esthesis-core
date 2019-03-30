package esthesis.platform.server.service;

import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.querydsl.core.types.Predicate;
import esthesis.platform.common.dto.BaseDTO;
import esthesis.platform.server.mapper.BaseMapper;
import esthesis.platform.server.model.BaseEntity;
import esthesis.platform.server.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
public abstract class BaseService<D extends BaseDTO, E extends BaseEntity> {

  @Autowired
  private BaseRepository<E> repository;

  @Autowired
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  private BaseMapper<D, E> mapper;

  public D save(D dto) {
    if (dto.getId() != null) {
      final E entity = ReturnOptional.r(repository.findById(dto.getId()));
      mapper.map(dto, entity);
      return dto;
    } else {
      E entity = mapper.map(dto);
      entity = repository.save(entity);
      return mapper.map(entity);
    }
  }

  public Page<D> findAll(Predicate predicate, Pageable pageable) {
    final Page<E> all = repository.findAll(predicate, pageable);

    return mapper.map(all);
  }

  public D findById(long id) {
    final E entity = ReturnOptional.r(repository.findById(id));

    return mapper.map(entity);
  }

  public E findEntityById(long id) {
    return ReturnOptional.r(repository.findById(id));
  }

  public D deleteById(long id) {
    final E entity = findEntityById(id);
    final D dto = mapper.map(entity);

    repository.deleteById(id);

    return dto;
  }
}
