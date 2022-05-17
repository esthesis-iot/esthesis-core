package esthesis.platform.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CampaignValidationErrorDTO {
  private List<String> main = new ArrayList<>();
  private List<String> devices = new ArrayList<>();
  private List<String> conditions = new ArrayList<>();

  public void addMainError(String err) {
    main.add(err);
  }

  public void addDevicesError(String err) {
    devices.add(err);
  }

  public void addConditionsError(String err) {
    conditions.add(err);
  }

  public void addConditionsError(int index, String err) {
    conditions.add("Condition " + index + ": " + err);
  }

  public boolean hasValidationErrors() {
    return main.size() > 0 || devices.size() > 0 || conditions.size() >0;
  }
}

