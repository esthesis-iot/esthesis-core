package esthesis.platform.server.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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

import java.util.Set;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Store extends BaseEntity {
  @NotNull
  @Size(max = 256)
  private String name;

  @NotNull
  @Size(max = 256)
  private String password;

  @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
  @JoinTable(name="store_cert_certificates",
    joinColumns={@JoinColumn(name="store_id")},
    inverseJoinColumns={@JoinColumn(name="certificate_id")})
  private Set<Certificate> certCertificates;

  @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
  @JoinTable(name="store_cert_cas",
    joinColumns={@JoinColumn(name="store_id")},
    inverseJoinColumns={@JoinColumn(name="ca_id")})
  private Set<Ca> certCas;

  @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
  @JoinTable(name="store_pk_certificates",
    joinColumns={@JoinColumn(name="store_id")},
    inverseJoinColumns={@JoinColumn(name="certificate_id")})
  private Set<Certificate> pkCertificates;

  @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
  @JoinTable(name="store_pk_cas",
    joinColumns={@JoinColumn(name="store_id")},
    inverseJoinColumns={@JoinColumn(name="ca_id")})
  private Set<Ca> pkCas;
}
