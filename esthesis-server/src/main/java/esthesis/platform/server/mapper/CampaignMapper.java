package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.CampaignDTO;
import esthesis.platform.server.model.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CampaignMapper extends BaseMapper<CampaignDTO, Campaign> {

  @Mapping(ignore = true, target = "modifiedBy")
  @Mapping(ignore = true, target = "modifiedOn")
  @Mapping(ignore = true, target = "createdBy")
  @Mapping(ignore = true, target = "version")
  @Mapping(ignore = true, target = "conditions")
  @Mapping(ignore = true, target = "members")
  public abstract Campaign map(CampaignDTO dto);

}

