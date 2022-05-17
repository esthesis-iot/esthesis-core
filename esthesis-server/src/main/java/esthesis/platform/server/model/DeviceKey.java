package esthesis.platform.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
@Table(name = "device_key")
public class DeviceKey extends BaseEntity {
  @Column(name = "public_key")
  private String publicKey;
  @Column(name = "private_key")
  private String privateKey;
  @Column(name = "rolled_on")
  private Instant rolledOn;
  @Column(name = "rolled_accepted")
  private boolean rolledAccepted;
  @ManyToOne
  private Device device;
  private String certificate;
  // The ID of the CA that signed device's certificate.
  @Column(name = "certificate_ca_id")
  private long certificateCaId;
}
