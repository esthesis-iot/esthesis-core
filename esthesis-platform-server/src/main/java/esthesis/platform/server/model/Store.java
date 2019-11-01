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

  @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
  @JoinTable(name="store_certificates",
    joinColumns={@JoinColumn(name="store_id")},
    inverseJoinColumns={@JoinColumn(name="certificate_id")})
  private Set<Certificate> certificates;

  @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
  @JoinTable(name="store_cas",
    joinColumns={@JoinColumn(name="store_id")},
    inverseJoinColumns={@JoinColumn(name="ca_id")})
  private Set<Ca> cas;
}
