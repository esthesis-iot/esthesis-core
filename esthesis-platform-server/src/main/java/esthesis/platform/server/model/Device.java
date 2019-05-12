package esthesis.platform.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
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

import java.util.List;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Device extends BaseEntity {
  private String hardwareId;

  @NotNull
  private String state;

  @Singular
  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<Tag> tags;

  @Singular
  @OneToMany(mappedBy="device", fetch = FetchType.LAZY)
  private List<DeviceKey> keys;
}
