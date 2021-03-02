package esthesis.platform.backend.server.workflow;

import static esthesis.platform.backend.server.workflow.CWFLConstants.VAR_LOOP_COUNTER;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;

/**
 * Helper methods for workflow tasks.
 */
public class ConditionsHelper {

  /**
   * Returns the campaign Id associated with this process instance.
   *
   * @param execution The currently executing instance.
   */
  public static long getCampaignId(DelegateExecution execution) {
    return Long.parseLong(execution.getVariable(CWFLConstants.VAR_CAMPAIGN_ID).toString());
  }

  /**
   * Returns the location of the workflow where the execution token currently is at (i.e. the name
   * of the subprocess encompassing the activity).
   * <p>
   * If the token is within a loop, the current value of the group counter is appended.
   * <p>
   * Examples: - global_entry - group_entry_1 - group_process_2 - etc.
   *
   * @param execution The currently executing instance.
   */
  public static String getTokenLocation(DelegateExecution execution) {
    String tokenLocation = execution.getParentActivityInstanceId();
    tokenLocation = tokenLocation.substring(0, tokenLocation.indexOf(":"));

    if (execution.getVariable(VAR_LOOP_COUNTER) != null && StringUtils
      .isNotBlank(execution.getVariable(VAR_LOOP_COUNTER).toString())) {
      tokenLocation += "_" + execution.getVariable(VAR_LOOP_COUNTER).toString();
    }

    return tokenLocation;
  }

  /**
   * Finds the target of the location where the workflow token currently resides at. The target
   * denotes the underlying group order, e.g. Global group is 0 and all other groups are 1, 2, 3,
   * etc.
   *
   * @param execution The currently executing instance.
   */
  public static int getTokenTarget(DelegateExecution execution) {
    String tokenLocation = getTokenLocation(execution);
    if (tokenLocation.equals(CWFLConstants.ACTIVITY_GLOBAL_ENTRY_ID) || tokenLocation
      .equals(CWFLConstants.ACTIVITY_GLOBAL_EXIT_ID)) {
      return 0;
    } else {
      return Integer.parseInt(tokenLocation.substring(tokenLocation.lastIndexOf("_") + 1)) + 1;
    }
  }
}
