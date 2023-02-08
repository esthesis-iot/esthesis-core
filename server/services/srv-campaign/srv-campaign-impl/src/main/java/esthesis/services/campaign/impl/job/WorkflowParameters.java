package esthesis.services.campaign.impl.job;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Data;

@Data
@RegisterForReflection
public class WorkflowParameters {

  public static final String MESSAGE_CONDITIONAL_PAUSE = "Message_ConditionalPause";

  private String campaignId;
  private Integer group;
  private String stage;

  private Integer minutes;
  private List<Integer> groups;
  private String timerExpression;

  private boolean rateCondition;
  private boolean remainingDevicesCondition;
  private boolean dateTimeCondition;
  private Integer pauseCondition;
  private boolean propertyCondition;

  // Advanced settings.
  private String advancedDateTimeRecheckTimer;
  private String advancedPropertyRecheckTimer;
  private String advancedUpdateRepliesTimer;
  private String advancedUpdateRepliesFinalTimer;
}
