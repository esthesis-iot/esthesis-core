package esthesis.service.crypto.dto;

import javax.ws.rs.core.MediaType;
import lombok.Data;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Data
public class ImportCaForm {

  @RestForm
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  public FileUpload backup;

}
