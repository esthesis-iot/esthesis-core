package esthesis.service.campaign.entity;

import esthesis.core.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@MongoEntity(collection = "CampaignDeviceMonitor")
public class CampaignDeviceMonitorEntity extends BaseEntity {

  private ObjectId deviceId;
  private String hardwareId;
  private ObjectId campaignId;
  private ObjectId commandRequestId;
  private ObjectId commandReplyId;
  private int group;
}
