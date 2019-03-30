package esthesis.platform.common.dto;

import esthesis.platform.common.config.DeviceInfoUnit;
import lombok.Data;

/** Provides placeholders for different information pertaining to a device */
@Data
public class DeviceInfo {
  // The name of this piece of info.
  private String n;

  // The value of this piece of info.
  private Object v;

  // The unit of this piece of info.
  private DeviceInfoUnit u;
}
