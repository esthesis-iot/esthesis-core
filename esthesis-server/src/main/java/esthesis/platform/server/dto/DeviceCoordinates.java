package esthesis.platform.server.dto;

import java.math.BigDecimal;
import java.time.Instant;

import esthesis.platform.server.dto.device.DeviceDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceCoordinates extends DeviceDTO  {
  BigDecimal lon;
  BigDecimal lat;
  Instant coordinatesUpdatedOn;
}
