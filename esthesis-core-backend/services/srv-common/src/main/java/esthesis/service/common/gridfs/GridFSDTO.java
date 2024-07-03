package esthesis.service.common.gridfs;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Builder
@Data
public class GridFSDTO {
	@NotEmpty
	private String metadataName;
	@NotEmpty
	private String metadataValue;
	@NotEmpty
	private String bucketName;
	@NotEmpty
	private String database;
	private FileUpload file;
}
