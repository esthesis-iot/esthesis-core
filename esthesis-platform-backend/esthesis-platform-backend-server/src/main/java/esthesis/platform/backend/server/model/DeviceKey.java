package esthesis.platform.backend.server.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;

@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DeviceKey extends BaseEntity {
  private String publicKey;
  private String privateKey;
  private Instant rolledOn;
  private String psPublicKey;
  private boolean rolledAccepted;
  @ManyToOne
  private Device device;
  private String sessionKey;
  private String provisioningKey;
  private String certificate;
  // The ID of the CA that signed device's certificate.
  private long certificateCaId;
}
