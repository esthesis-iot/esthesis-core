package esthesis.platform.backend.server.model;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CommandRequest extends BaseEntity {

  @NotNull
  private String operation;
  private String args;
  private String description;
  @Singular
  @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
  private Device device;
  @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
  private Campaign campaign;

}
