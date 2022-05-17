package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.CampaignConditionDTO;
import esthesis.platform.server.model.CampaignCondition;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CampaignConditionMapper {

  public abstract CampaignCondition map(CampaignConditionDTO dto);
  public abstract CampaignConditionDTO map(CampaignCondition entity);

  public abstract List<CampaignConditionDTO> map(List<CampaignCondition> byCampaignIdAndTargetAndStageAndType);
}
