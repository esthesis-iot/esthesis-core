package esthesis.platform.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@Table(name = "nifi_sink")
public class NiFiSink extends BaseEntity {

  @NotNull
  private String name;

  @NotNull
  @Column(name = "factory_class")
  private String factoryClass;
  private int handler;
  private boolean state;
  private String configuration;
  @Column(name = "custom_info")
  private String customInfo;
  private String type;
  @Column(name = "validation_errors")
  private String validationErrors;
}
