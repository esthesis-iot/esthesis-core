package esthesis.device.runtime.health;

import lombok.Data;

import java.util.List;

@Data
public class DeviceHealthDataDTO {
  // The temperature of the CPU.
  private double cpuTemperature;

  // Memory currently available.
  private long memoryAvailable;
  // Total memory.
  private long memoryTotal;

  // 1-minute load.
  private double load1;
  // 5-minutes load.
  private double load5;
  // 15-minutes load.
  private double load15;

  // A comma-separated list of available filesystems:
  // filesystem-name,remaining-space,total-space.
  private List<String> filesystems;

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
