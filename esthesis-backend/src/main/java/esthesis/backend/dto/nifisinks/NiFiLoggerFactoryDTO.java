package esthesis.backend.dto.nifisinks;

import esthesis.common.device.dto.BaseDTO;
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
public class NiFiLoggerFactoryDTO extends BaseDTO {

  private String factoryClass;
  private String friendlyName;
  private String version;
  private String configurationTemplate;
  private boolean supportsSyslogLog;
  private boolean supportsFilesystemLog;
}
