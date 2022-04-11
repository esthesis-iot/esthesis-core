package esthesis.platform.backend.server.repository;

import esthesis.platform.backend.server.model.CampaignMember;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignMemberRepository extends CrudRepository<CampaignMember, Long> {
  List<CampaignMember> findByCampaignIdAndGroupOrder(long campaignId, int groupOrd);
  List<CampaignMember> findByCampaignId(long campaignId);
}
