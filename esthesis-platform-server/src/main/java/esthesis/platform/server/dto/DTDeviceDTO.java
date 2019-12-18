package esthesis.platform.server.dto;

import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DTDeviceDTO extends DeviceDTO {
}
