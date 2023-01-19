package esthesis.util.kogito.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class InstanceDTO {

  private String id;
  private Map<String, String> data = new HashMap<>();

}
