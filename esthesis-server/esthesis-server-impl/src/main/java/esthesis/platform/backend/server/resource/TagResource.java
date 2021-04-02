package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.eurodyn.qlack.util.validation.QValidationUtil;
import com.querydsl.core.types.Predicate;
import esthesis.platform.backend.server.dto.TagDTO;
import esthesis.platform.backend.server.model.Tag;
import esthesis.platform.backend.server.service.TagService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Validated
@RestController
@RequestMapping("/tags")
public class TagResource {

  private final TagService tagService;

  public TagResource(TagService tagService) {
    this.tagService = tagService;
  }

  /**
   * Returns the profile of the current application.
   *
   * @return Returns the profile of the current application
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not retrieve tags list.")
  @EmptyPredicateCheck
  public Page<TagDTO> findAll(@QuerydslPredicate(root = Tag.class) Predicate predicate,
    Pageable pageable) {
    return tagService.findAll(predicate, pageable);
  }

  /**
   * Saves an application.
   *
   * @param tagDTO The application to save
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save tag.",
    ignoreValidationExceptions = true)
  public TagDTO save(@Valid @RequestBody TagDTO tagDTO, Errors errors,
    BindingResult bindingResult) throws MethodArgumentNotValidException {
    // Check tag name does not exist.
    final Optional<Tag> existingTag = tagService.findByName(tagDTO.getName());
    if (((tagDTO.getId() == null && existingTag.isPresent()) ||
      ((tagDTO.getId() != null && !existingTag.orElseThrow().getId().equals(tagDTO.getId()))))) {
      new QValidationUtil(errors, bindingResult)
        .throwValidationError("name", "TAG_ALREADY_EXISTS", "Tag name already exists.");
    }

    // Save tag.
    return tagService.save(tagDTO);
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch tag.")
  public TagDTO get(@PathVariable long id) {
    return tagService.findById(id);
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete tag.")
  public void delete(@PathVariable long id) {
    tagService.deleteById(id);
  }

}
