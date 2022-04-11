package esthesis.platform.backend.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CampaignStatsDTO {
  private List<Integer> groupMembers;
  private List<Integer> groupMembersReplied;
  private int allMembers;
  private int membersContacted;
  private int membersContactedButNotReplied;
  private int membersReplied;
  private int successRate;
  private int progress;
  private String duration;
  private String eta;
}
