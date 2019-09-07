package esthesis.device.runtime.model;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@NoArgsConstructor
@Document(collection = "provisioning", schemaVersion= "1.0")
public class Provisioning implements Serializable {
  @Id
  private String id;
  private String name;
  private String filename;
  private long packageId;
  private String version;
  private String sha256;
  private boolean isInitialProvisioning;
  private Date provisionedOn;
}
