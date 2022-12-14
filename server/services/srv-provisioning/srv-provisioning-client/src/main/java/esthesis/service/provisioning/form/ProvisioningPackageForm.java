package esthesis.service.provisioning.form;

import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import javax.ws.rs.core.MediaType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProvisioningPackageForm extends ProvisioningPackageEntity {

  @RestForm
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  public FileUpload file;
}
