package esthesis.services.tag.service;

import esthesis.dto.Tag;
import esthesis.service.BaseService;
import esthesis.services.tag.repository.TagRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class TagService extends BaseService<Tag> {

  @Inject
  JsonWebToken jwt;

  @Inject
  TagRepository tagRepository;

//  public Optional<Tag> findByName(String name) {
//    return tagRepository.findByName(name);
//  }
//
//  public Iterable<Tag> findAllByNameIn(List<String> names) {
//    return tagRepository.findAllByNameIn(names);
//  }
//
//  public Iterable<TagDTO> findAllById(List<Long> ids) {
//    return tagMapper.map(tagRepository.findAllByIdIn(ids));
//  }
//
//  public Iterable<Tag> findAllEntityById(List<Long> ids) {
//    return tagRepository.findAllByIdIn(ids);
//  }
}
