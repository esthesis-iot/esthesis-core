package esthesis.platform.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class DataSinkFactoryDTO {
  private String factoryClass;
  private String friendlyName;
  private boolean supportsMetadataForRead;
  private boolean supportsTelemetryForRead;
  private boolean supportsMetadataForWrite;
  private boolean supportsTelemetryForWrite;
  private String version;
  private String configurationTemplate;
}
