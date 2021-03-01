package esthesis.platform.backend.server.dto;

import esthesis.platform.backend.common.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Collection;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CampaignDTO extends BaseDTO {
  private String name;
  private Integer type;
  private Integer state;
  private String description;
  private String commandName;
  private String commandArguments;
  private String provisioningPackageId;
  private Collection<CampaignConditionDTO> conditions;
  private Collection<CampaignMemberDTO> members;
  private Instant startedOn;
  private Instant terminatedOn;
  private Long provisioningId;
}
