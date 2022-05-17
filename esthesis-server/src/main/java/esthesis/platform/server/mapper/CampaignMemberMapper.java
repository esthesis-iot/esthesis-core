package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.CampaignMemberDTO;
import esthesis.platform.server.model.CampaignMember;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CampaignMemberMapper {
  public abstract CampaignMember map(CampaignMemberDTO dto);
  public abstract CampaignMemberDTO map(CampaignMember entity);
  public abstract List<CampaignMemberDTO> map(List<CampaignMember> campaignMembers);
}
