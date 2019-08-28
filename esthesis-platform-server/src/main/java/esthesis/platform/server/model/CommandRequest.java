package esthesis.platform.server.model;

import esthesis.common.config.AppConstants;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
  private AppConstants.MqttCommand command;

  private String description;

  @Singular
  @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
  private Device device;
}
