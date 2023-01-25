package esthesis.service.campaign.dto;

import esthesis.common.AppConstants.Campaign.Condition.Stage;
import esthesis.common.exception.QMismatchException;
import lombok.Data;

@Data
public class GroupDTO {

  private Stage stage;
  private int group;
  public static final String GLOBAL_GROUP = "GLOBAL";

  public GroupDTO(String groupInfo) {
    String[] split = groupInfo.split(":");
    if (split.length != 3) {
      throw new QMismatchException("Invalid group name '{}'.", groupInfo);
    }

    group = Integer.parseInt(split[1]);

    if (split[2].equalsIgnoreCase("entry")) {
      stage = stage.ENTRY;
    } else if (split[2].equalsIgnoreCase("exit")) {
      stage = stage.EXIT;
    } else if (split[2].equalsIgnoreCase("inside")) {
      stage = stage.INSIDE;
    } else {
      throw new QMismatchException("Invalid group phase for group '{}'.", groupInfo);
    }
  }
}

