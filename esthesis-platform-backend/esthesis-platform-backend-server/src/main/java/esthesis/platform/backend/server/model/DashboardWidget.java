package esthesis.platform.backend.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DashboardWidget extends BaseEntity {
  private String type;
  private int gridCols;
  private int gridRows;
  private String configuration;
  @Column(name = "grid_x")
  private int gridX;
  @Column(name = "grid_y")
  private int gridY;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Dashboard dashboard;
  // Data update frequency in seconds.
  private int updateEvery;

}
