package esthesis.platform.server.service;

import esthesis.platform.server.dto.TagDTO;
import esthesis.platform.server.model.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
public class TagService extends BaseService<TagDTO, Tag> {

}
