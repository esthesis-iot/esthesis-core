package esthesis.device.runtime.model;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "registration", schemaVersion= "1.0")
public class Registration {
  @Id
  private String id;
  @NotNull
  private String provisioningUrl;
  @NotNull
  private Date registeredOn;
  private String mqttServerIp;
}
