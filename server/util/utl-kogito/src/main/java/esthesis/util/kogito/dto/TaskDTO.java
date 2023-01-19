package esthesis.util.kogito.dto;

import lombok.Data;

@Data
public class TaskDTO {

  private String id;
  private String name;
  private Integer state;
  private String phase;
  private String phaseStatus;
}
