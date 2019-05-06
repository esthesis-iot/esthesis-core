package esthesis.platform.server.service;

import esthesis.platform.server.dto.TagDTO;
import esthesis.platform.server.model.Tag;
import esthesis.platform.server.repository.TagRepository;
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

  public TagService(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  public Optional<Tag> findByName(String name) {
    return tagRepository.findByName(name);
  }

  public Iterable<Tag> findAllByNameIn(List<String> names) {
    return tagRepository.findAllByNameIn(names);
  }

}
