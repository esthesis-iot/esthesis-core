package esthesis.platform.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "ca")
public class Ca extends BaseEntity {

  @NotNull
  @Size(max = 256)
  @Column(updatable = false)
  private String cn;

  @Column(updatable = false)
  private Instant issued;

  @NotNull
  @Column(updatable = false)
  private Instant validity;

  @NotNull
  @Column(updatable = false, name = "public_key")
  private String publicKey;

  @NotNull
  @Column(updatable = false, name = "private_key")
  private String privateKey;

  @NotNull
  @Column(updatable = false)
  private String certificate;

  // The CN of the parent CA.
  @Column(name = "parent_ca")
  private String parentCa;
}
