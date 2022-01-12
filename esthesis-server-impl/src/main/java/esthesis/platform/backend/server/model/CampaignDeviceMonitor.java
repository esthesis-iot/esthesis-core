package esthesis.platform.backend.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
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
public class CampaignDeviceMonitor extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  private Device device;
  @OneToOne(fetch = FetchType.LAZY)
  private Campaign campaign;
  private Long commandRequestId;
  @OneToOne(fetch = FetchType.LAZY)
  private CommandReply commandReply;
  private int groupOrder;
}
