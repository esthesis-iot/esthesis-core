package esthesis.services.campaign.impl.worker;

import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SetGroupWorker extends BaseWorker {

  /**
   * Creates a "group expression" based on the currently processed group. The expression has the
   * following format: "group" + group position + stage, e.g. group:1:entry, group:2:exit, etc. The
   * global group is denoted with "0", e.g. group:0:entry.
   * <p>
   * This expression is used to allow condition-checkers to find which conditions should be checked
   * at each step of the workflow.
   *
   * @param campaignId    The id of the campaign represented by the currently executing workflow.
   * @param groupPosition The position of the group, 0 denoting the global group.
   * @param groupPhase    The phase of the group, entry or exit.
   */
  public String setGroup(String campaignId, int groupPosition, String groupPhase) {
    log.trace("setGroup, campaignId: {}, groupPosition: {}, groupPhase: {}", campaignId,
        groupPosition, groupPhase);
    String group = "group:" + groupPosition + ":" + groupPhase;
    log.debug("Setting group to '{}'.", group);
    setStateDescription(campaignId, "Settings group.");

    return group;
  }
}
