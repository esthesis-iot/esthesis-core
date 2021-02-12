package esthesis.platform.backend.server.repository;

import com.querydsl.jpa.impl.JPAQuery;
import esthesis.platform.backend.server.model.*;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public interface CampaignRepository extends BaseRepository<Campaign>,
  QuerydslPredicateExecutor<Campaign>, CampaignRepositoryExt {

}

interface CampaignRepositoryExt {

  int countCampaignGroups(long campaignId);
}

class CampaignRepositoryImpl implements CampaignRepositoryExt {

  @PersistenceContext
  private EntityManager em;
  private static final QCampaign campaign = QCampaign.campaign;
  private static final QCampaignMember campaignMember = QCampaignMember.campaignMember;

  @Override
  public int countCampaignGroups(long campaignId) {
    return (int)(new JPAQuery<DeviceKey>(em)
      .select(campaignMember.groupOrder)
      .from(campaignMember)
      .where(campaignMember.campaign.id.eq(campaignId))
      .distinct()
      .fetchCount());
  }
}
