package esthesis.platform.server.repository;

import esthesis.platform.server.model.CampaignCondition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignConditionRepository extends CrudRepository<CampaignCondition, Long> {
  List<CampaignCondition> findByCampaignIdAndTargetAndStageAndType(long campaign, int target, int stage, int type);
  List<CampaignCondition> findByCampaignIdAndTargetAndType(long campaign, int target, int type);
}
