package esthesis.common.datasink.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class DataSinkQueryResult {
  private String hardwareId;
  private String measurement;
  private List<String> columns;
  private List<List<Object>> values;
}
