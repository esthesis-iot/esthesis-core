package esthesis.platform.server.model;

import javax.persistence.Entity;
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
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Accessors(chain = true)
public class Provisioning extends BaseEntity {

  @NotNull
  private String name;
  private String description;
  private boolean state;
  private boolean defaultIP;
  @Singular
  @OneToMany
  @JoinTable(inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<Tag> tags;
  @NotNull
  private String packageVersion;
  private byte[] fileContent;
  private long fileSize;
  private String fileName;
}
