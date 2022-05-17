package esthesis.platform.server.model;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
@Table(name = "provisioning")
public class Provisioning extends BaseContentEntity {

  @NotNull
  private String name;
  private String description;
  private boolean state;
  @Singular
  @OneToMany(fetch = LAZY)
  @JoinTable(
    joinColumns = @JoinColumn(name = "provisioning_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id"), name = "provisioning_tags")
  private List<Tag> tags;

  @NotNull
  @Column(name = "package_version")
  private String packageVersion;
  @Column(name = "file_name")
  private String fileName;
  // The SHA256 digest of the provisioning package.
  private String sha256;
}
