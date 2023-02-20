package esthesis.service.campaign.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CampaignStatsDTO {

  private List<Long> groupMembers;
  private List<Long> groupMembersReplied;
  private long allMembers;
  private long membersContacted;
  private long membersContactedButNotReplied;
  private long membersReplied;
  private BigDecimal successRate;
  private BigDecimal progress;
  private String duration;
  private String stateDescription;
}
