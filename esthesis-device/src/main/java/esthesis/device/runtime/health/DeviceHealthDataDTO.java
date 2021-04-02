package esthesis.device.runtime.health;

import lombok.Data;

@Data
public class DeviceHealthDataDTO {
  // Device's time as EPOCH (msec).
  private long currentTime;

  // Device's uptime in msec.
  private long upTime;

  // Device's IP address.
  private String ipAddress;

  // Device's runtime agent version.
  private String runtimeVersion;
  private String runtimeCommitId;

  // Firmware version.
  private String firmwareVersion;
}
