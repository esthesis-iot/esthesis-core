package esthesis.dataflows.oriongateway.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrionQueryDTO implements Serializable {

  private Expression expression;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Expression implements Serializable {

    private String q;
  }

}
