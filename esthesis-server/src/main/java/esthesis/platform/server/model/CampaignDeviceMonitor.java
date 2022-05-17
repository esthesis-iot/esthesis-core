package esthesis.platform.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
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
@Table(name = "campaign_device_monitor")
public class CampaignDeviceMonitor extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  private Device device;
  @OneToOne(fetch = FetchType.LAZY)
  private Campaign campaign;
  @Column(name = "command_request_id")
  private Long commandRequestId;
  //DBTODO
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="command_reply_id")
  private CommandReply commandReply;
  @Column(name = "group_order")
  private int groupOrder;
}
