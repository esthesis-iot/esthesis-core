package esthesis.service.campaign.dto;

import esthesis.core.common.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A representation of a campaign member (device or tag).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CampaignMemberDTO {

	// The identifier of this member, either a device hardware id or a tag name.
	private String identifier;

	// The group this member belongs to.
	private int group;

	// The type of this member, either a device or a tag.
	private AppConstants.Campaign.Member.Type type;
}
