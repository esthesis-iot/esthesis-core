package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.CampaignMemberDTO;
import esthesis.platform.backend.server.model.CampaignMember;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CampaignMemberMapper {
  public abstract CampaignMember map(CampaignMemberDTO dto);
  public abstract CampaignMemberDTO map(CampaignMember entity);
  public abstract List<CampaignMemberDTO> map(List<CampaignMember> campaignMembers);
}
