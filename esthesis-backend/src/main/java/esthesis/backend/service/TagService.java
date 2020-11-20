package esthesis.backend.service;

import esthesis.backend.mapper.TagMapper;
import esthesis.backend.repository.TagRepository;
import esthesis.backend.dto.TagDTO;
import esthesis.backend.model.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional
public class TagService extends BaseService<TagDTO, Tag> {
  private final TagRepository tagRepository;
  private final TagMapper tagMapper;

  public TagService(TagRepository tagRepository,
    TagMapper tagMapper) {
    this.tagRepository = tagRepository;
    this.tagMapper = tagMapper;
  }

  public Optional<Tag> findByName(String name) {
    return tagRepository.findByName(name);
  }

  public Iterable<Tag> findAllByNameIn(List<String> names) {
    return tagRepository.findAllByNameIn(names);
  }

  public Iterable<TagDTO> findAllById(List<Long> ids) {
    return tagMapper.map(tagRepository.findAllByIdIn(ids));
  }

  public Iterable<Tag> findAllEntityById(List<Long> ids) {
    return tagRepository.findAllByIdIn(ids);
  }

}
