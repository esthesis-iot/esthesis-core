package esthesis.service.provisioning.dto;

import esthesis.common.dto.BaseDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ProvisioningPackageBinary extends BaseDTO {

  // A reference to the provisioning package this binary belongs to.
  private ObjectId provisioningPackage;

  private byte[] payload;
}
