package esthesis.platform.server.model;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@ToString
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "campaign")
public class Campaign extends BaseEntity {

  @NotNull
  private String name;
  private Integer state;
  @NotNull
  private int type;
  private String description;
  @Column(name = "command_name")
  private String commandName;
  @Column(name = "command_arguments")
  private String commandArguments;
  @Column(name = "provisioning_id")
  private Long provisioningId;
  @Column(name = "started_on")
  private Instant startedOn;
  @Column(name = "terminated_on")
  private Instant terminatedOn;
  @Column(name = "state_description")
  private String stateDescription;
  @Column(name = "process_instance_id")
  private String processInstanceId;

  @OneToMany(mappedBy = "campaign", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CampaignCondition> conditions = new ArrayList<>();

  @OneToMany(mappedBy = "campaign", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CampaignMember> members = new ArrayList<>();
}
