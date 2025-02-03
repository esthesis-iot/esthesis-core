package esthesis.service.campaign.dto;

import esthesis.core.common.AppConstants;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A representation of campaign statistics.
 */
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
	private AppConstants.Campaign.State state;
	private Instant createdOn;
	private Instant startedOn;
	private Instant terminatedOn;
}
