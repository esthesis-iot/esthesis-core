package esthesis.platform.server.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
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

import java.util.List;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Accessors(chain = true)
public class Virtualization extends BaseEntity {
  @NotNull
  private String name;

  @NotNull
  private String ipAddress;

  @NotNull
  private int serverType;

  @NotNull
  private boolean state;

  @NotNull
  private int security;

  @OneToOne
  private Certificate certificate;

  @Singular
  @OneToMany
  @JoinTable(inverseJoinColumns=@JoinColumn(name="tag_id"))
  private List<Tag> tags;
}
