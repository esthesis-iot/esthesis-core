package esthesis.platform.backend.server.model;

import static javax.persistence.FetchType.LAZY;

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
public class Provisioning extends BaseContentEntity {

  @NotNull
  private String name;
  private String description;
  private boolean state;
  @Singular
  @OneToMany(fetch = LAZY)
  @JoinTable(inverseJoinColumns = @JoinColumn(name = "tag_id"), name = "provisioning_tags")
  private List<Tag> tags;
  @NotNull
  private String packageVersion;
  private String fileName;
  // The signature of the unencrypted file.
  private String signaturePlain;
  // The signature of the encrypted file.
  private String signatureEncrypted;

  // An indicator that the file has finished being encrypted.
  private boolean encrypted;

  // The SHA256 digest of the unencrypted version of this provisioning package.
  private String sha256;
}
