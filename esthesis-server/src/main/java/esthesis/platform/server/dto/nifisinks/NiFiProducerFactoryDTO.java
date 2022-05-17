package esthesis.platform.server.dto.nifisinks;

import esthesis.platform.server.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class NiFiProducerFactoryDTO extends BaseDTO {

  private String factoryClass;
  private String friendlyName;
  private String version;
  private boolean supportsTelemetryProduce;
  private boolean supportsMetadataProduce;
  private boolean supportsCommandProduce;
  private String configurationTemplate;
}
