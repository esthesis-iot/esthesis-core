package esthesis.services.campaign.impl.service;

import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.common.BaseService;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

@Slf4j
@ApplicationScoped
public class CampaignDeviceMonitorService extends BaseService<CampaignDeviceMonitorEntity> {

  public long countReplies(ObjectId campaignId, int group) {
    findByColumn("a", "a");
    return getRepository().count("campaignId = ?1 and group = ?2 and commandRequestId is not null"
        + " and commandReplyId is not null", campaignId, group);
  }

  public long countContactedNotReplied(ObjectId campaignId) {
    return getRepository().count("campaignId = ?1 and commandRequestId is not null"
        + " and commandReplyId is null", campaignId);
  }

  public long countReplies(ObjectId campaignId) {
    return getRepository().count("campaignId = ?1 and commandRequestId is not null"
        + " and commandReplyId is not null", campaignId);
  }

  public long countContacted(ObjectId campaignId) {
    return getRepository().count("campaignId = ?1 and commandRequestId is not null", campaignId);
  }

  public long countAll(ObjectId campaignId) {
    return getRepository().count("campaignId = ?1", campaignId);
  }

  public List<CampaignDeviceMonitorEntity> findByCampaignID(String campaignId) {
    return findByColumn("campaignId", campaignId);
  }
}
