package esthesis.platform.server.model;

import com.querydsl.core.annotations.QuerySupertype;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;
import org.springframework.content.commons.annotations.ContentId;
import org.springframework.content.commons.annotations.ContentLength;

@Data
@QuerySupertype
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
@OptimisticLocking(type = OptimisticLockType.VERSION)
public abstract class BaseContentEntity extends BaseEntity {

  @ContentId
  @Column(name = "content_id")
  private String contentId;
  @ContentLength
  @Column(name = "file_size")
  private long fileSize;
}
