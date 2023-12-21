package esthesis.service.crypto.form;

import jakarta.ws.rs.core.MediaType;
import lombok.Data;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Data
public class ImportCaForm {

	@RestForm
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	public FileUpload publicKey;

	@RestForm
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	public FileUpload privateKey;

	@RestForm
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	public FileUpload certificate;

	@RestForm
	public String name;

}
