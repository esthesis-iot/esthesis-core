package esthesis.platform.server.model;

import com.eurodyn.qlack.util.data.encryption.Encrypted;
import javax.persistence.Entity;
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

import java.time.Instant;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Accessors(chain = true)
public class TunnelServer extends BaseEntity {

  @NotNull
  private String ip;

  @NotNull
  private String token;

  private String publicKey;

  private String privateKey;

  private String certificate;

  @NotNull
  private boolean status;

  @NotNull
  private String name;

  @NotNull
  private String parentCa;

  @NotNull
  private Instant validity;
}
