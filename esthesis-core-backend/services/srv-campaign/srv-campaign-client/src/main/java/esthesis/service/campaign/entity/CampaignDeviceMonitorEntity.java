package esthesis.service.campaign.entity;

import esthesis.core.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;


/**
 * An entity to store the device monitor data for a campaign.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "CampaignDeviceMonitor")
public class CampaignDeviceMonitorEntity extends BaseEntity {

	private ObjectId deviceId;
	private String hardwareId;
	private ObjectId campaignId;
	private ObjectId commandRequestId;
	private ObjectId commandReplyId;
	private int group;
}
