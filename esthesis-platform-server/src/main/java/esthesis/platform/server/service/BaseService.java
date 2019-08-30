package esthesis.platform.server.service;

import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.dto.BaseDTO;
import esthesis.platform.server.mapper.BaseMapper;
import esthesis.platform.server.model.BaseEntity;
import esthesis.platform.server.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.content.commons.repository.ContentStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.InputStream;
import java.util.List;

@Service
@Validated
@Transactional
abstract class BaseService<D extends BaseDTO, E extends BaseEntity> {

  @Autowired
  private BaseRepository<E> repository;

  @Autowired
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  private BaseMapper<D, E> mapper;

  @Autowired(required = false)
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  private ContentStore<E, String> contentStore;

  public D save(D dto, InputStream file) {
    if (dto.getId() != null && dto.getId() != 0) {
      final E entity = ReturnOptional.r(repository.findById(dto.getId()));
      mapper.map(dto, entity);
      return dto;
    } else {
      E entity = mapper.map(dto);
      contentStore.setContent(entity, file);
      entity = repository.save(entity);
      return mapper.map(entity);
    }
  }

  public D save(D dto) {
    E entity;

    if (dto.getId() != null && dto.getId() != 0) {
      entity = ReturnOptional.r(repository.findById(dto.getId()));
      mapper.map(dto, entity);
    } else {
      entity = mapper.map(dto);
      entity = repository.save(entity);
    }

    return mapper.map(entity);
  }

  public Page<D> findAll(Predicate predicate, Pageable pageable) {
    final Page<E> all = repository.findAll(predicate, pageable);

    return mapper.map(all);
  }

  public List<D> findAll() {
    return mapper.map(repository.findAll());
  }

  public D findById(long id) {
    return mapper.map(ReturnOptional.r(repository.findById(id)));
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
