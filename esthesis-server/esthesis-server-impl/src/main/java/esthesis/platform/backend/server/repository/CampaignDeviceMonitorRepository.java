package esthesis.platform.backend.server.repository;

import com.querydsl.jpa.impl.JPAQuery;
import esthesis.platform.backend.server.model.CampaignDeviceMonitor;
import esthesis.platform.backend.server.model.QCampaignDeviceMonitor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public interface CampaignDeviceMonitorRepository extends BaseRepository<CampaignDeviceMonitor>,
  CampaignDeviceMonitorRepositoryExt {

  List<CampaignDeviceMonitor> findAllByCampaignIdAndCommandRequestIdNotNullAndCommandReplyNull(
    long campaignId);

  int countAllByCampaignId(long campaignId);

  int countAllByCampaignIdAndCommandRequestIdNotNull(long campaignId);

  int countAllByCampaignIdAndCommandRequestIdNotNullAndCommandReplyNull(long campaignId);

  int countAllByCampaignIdAndCommandRequestIdNotNullAndCommandReplyNotNull(long campaignId);

  int countAllByCampaignIdAndGroupOrderAndCommandRequestIdNotNullAndCommandReplyNotNull(
    long campaignId, int groupOrder);

  int countByCampaignIdAndCommandRequestIdNull(long campaignId);
}

interface CampaignDeviceMonitorRepositoryExt {

  List<CampaignDeviceMonitor> findNextBatch(long campaignId, int groupOrder, int batchSize);

  int findRateAsNumber(long campaignId, int groupOrder);

  int findRateAsPercentage(long campaignId, int groupOrder);
}

class CampaignDeviceMonitorRepositoryImpl implements CampaignDeviceMonitorRepositoryExt {

  @PersistenceContext
  private EntityManager em;
  private static final QCampaignDeviceMonitor cdm = QCampaignDeviceMonitor.campaignDeviceMonitor;

  @Override
  public List<CampaignDeviceMonitor> findNextBatch(long campaignId, int groupOrder, int batchSize) {
    return new JPAQuery<CampaignDeviceMonitor>(em)
      .from(cdm)
      .where(
        cdm.campaign.id.eq(campaignId)
          .and(cdm.groupOrder.eq(groupOrder))
          .and(cdm.commandRequestId.isNull())
      )
      .orderBy(cdm.id.asc())
      .limit(batchSize)
      .fetch();
  }

  @Override
  public int findRateAsNumber(long campaignId, int groupOrder) {
    return (int)
      new JPAQuery<CampaignDeviceMonitor>(em)
        .from(cdm)
        .where(
          cdm.campaign.id.eq(campaignId)
            .and(cdm.groupOrder.eq(groupOrder))
            .and(cdm.commandReply.isNotNull())
        )
        .fetchCount();
  }

  @Override
  public int findRateAsPercentage(long campaignId, int groupOrder) {
    return (int)
      ((new JPAQuery<CampaignDeviceMonitor>(em)
        .from(cdm)
        .where(
          cdm.campaign.id.eq(campaignId)
            .and(cdm.groupOrder.eq(groupOrder))
            .and(cdm.commandReply.isNotNull())
        )
        .fetchCount()
        /
        new JPAQuery<CampaignDeviceMonitor>(em)
          .from(cdm)
          .where(
            cdm.campaign.id.eq(campaignId)
              .and(cdm.groupOrder.eq(groupOrder))
              .and(cdm.commandRequestId.isNotNull())
          )
          .fetchCount()) * 100);
  }
}
