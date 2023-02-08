package esthesis.services.campaign.impl.dto;

import esthesis.common.AppConstants.Campaign.Condition.Stage;
import esthesis.services.campaign.impl.job.WorkflowParameters;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import lombok.Data;

@Data
public class GroupDTO {

  private Stage stage;
  private int group;
  public static final String GLOBAL_GROUP = "GLOBAL";

  public GroupDTO(ActivatedJob job) {
    WorkflowParameters p = job.getVariablesAsType(WorkflowParameters.class);

    group = p.getGroup();
    stage = Stage.valueOf(p.getStage());
  }

  public GroupDTO(Stage stage, int group) {
    this.stage = stage;
    this.group = group;
  }

  @Override
  public String toString() {
    return "group: " + group + ", stage:" + stage;
  }
}

