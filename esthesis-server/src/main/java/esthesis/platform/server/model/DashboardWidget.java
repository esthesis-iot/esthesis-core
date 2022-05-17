package esthesis.platform.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
@Table(name = "dashboard_widget")
public class DashboardWidget extends BaseEntity {
  private String type;
  @Column(name = "grid_cols")
  private int gridCols;
  @Column(name = "grid_rows")
  private int gridRows;
  private String configuration;
  @Column(name = "grid_x")
  private int gridX;
  @Column(name = "grid_y")
  private int gridY;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Dashboard dashboard;
  // Data update frequency in seconds.
  @Column(name = "update_every")
  private int updateEvery;

}
