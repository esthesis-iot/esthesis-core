package esthesis.service.device.dto;

import esthesis.common.dto.GenericRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class ProvisioningRequest implements GenericRequest {

  private Long provisioningPackageId;

}
