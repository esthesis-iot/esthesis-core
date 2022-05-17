package esthesis.platform.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "campaign_condition")
public class CampaignCondition {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private int type;
  private int target;
  private Integer stage;
  private String value;
  @Column(name = "property_name")
  private String propertyName;
  @Column(name = "schedule_date")
  private Instant scheduleDate;
  private Integer operation;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Campaign campaign;
}
