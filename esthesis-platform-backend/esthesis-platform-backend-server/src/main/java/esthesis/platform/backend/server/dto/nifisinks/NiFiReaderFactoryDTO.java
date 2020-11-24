package esthesis.platform.backend.server.dto.nifisinks;

import esthesis.platform.backend.common.dto.BaseDTO;
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
public class NiFiReaderFactoryDTO extends BaseDTO {

  private String factoryClass;
  private String friendlyName;
  private String version;
  private String configurationTemplate;
  private boolean supportsPingRead;
  private boolean supportsMetadataRead;
  private boolean supportsTelemetryRead;
  private boolean supportsCommandRead;
}
