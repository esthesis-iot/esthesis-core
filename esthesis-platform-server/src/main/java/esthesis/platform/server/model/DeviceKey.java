package esthesis.platform.server.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
public class DeviceKey extends BaseEntity {
  private String publicKey;
  private String privateKey;
  private Instant rolledOn;
  private String psPublicKey;
  private boolean rolledAccepted;
  @ManyToOne
  @JoinColumn(name="device_id", nullable=false)
  private Device device;
  private String sessionKey;
  private String provisioningKey;
}
