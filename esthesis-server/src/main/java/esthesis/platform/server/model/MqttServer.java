package esthesis.platform.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Data
@Entity
@ToString
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "mqtt_server")
public class MqttServer extends BaseEntity {

  @NotNull
  private String name;
  @NotNull
  @Column(name = "ip_address")
  private String ipAddress;
  @NotNull
  private boolean state;
  @Singular
  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(
    joinColumns = @JoinColumn(name = "mqtt_server_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id"), name = "mqtt_server_tags")
  private List<Tag> tags;
  private String description;
}
