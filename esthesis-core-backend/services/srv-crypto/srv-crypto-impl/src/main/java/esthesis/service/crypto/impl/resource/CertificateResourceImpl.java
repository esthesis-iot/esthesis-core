package esthesis.service.crypto.impl.resource;

import com.github.slugify.Slugify;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.impl.service.CertificateService;
import esthesis.service.crypto.resource.CertificateResource;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * Implementation of the @{@link CertificateResource} interface.
 */
@Authenticated
public class CertificateResourceImpl implements CertificateResource {

	@Inject
	@RestClient
	SettingsResource settingsResource;

	@Inject
	CertificateService certificateService;

	@GET
	@Override
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	@JSONReplyFilter(filter = "content,content.id,content.cn,content.issued,content.issuer,content"
		+ ".validity,content.name")
	@Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "Search certificates", log =
		AuditLogType.DATA_IN)
	public Page<CertificateEntity> find(@BeanParam Pageable pageable) {
		return certificateService.find(pageable);
	}

	@GET
	@Override
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	@JSONReplyFilter(filter = "id,cn,issued,validity,issuer,san,name")
	@Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "View certificate")
	public CertificateEntity findById(String id) {
		return certificateService.findById(id);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public CertificateEntity findByIdComplete(String id) {
		return findById(id);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "Download certificate", log =
		AuditLogType.DATA_IN)
	public Response download(String certId, AppConstants.KeyType type) {
		CertificateEntity cert = certificateService.findById(certId);

		String content;
		String filename = Slugify.builder().underscoreSeparator(true).build().slugify(cert.getCn());
		switch (type) {
			case PRIVATE -> {
				filename += ".key";
				content = certificateService.getPrivateKey(certId);
			}
			case PUBLIC -> {
				filename += ".pub";
				content = certificateService.getPublicKey(certId);
			}
			case CERTIFICATE -> {
				filename += ".crt";
				content = certificateService.getCertificate(certId);
			}
			default -> throw new QDoesNotExistException("Key type {} is not valid.", type);
		}
		return ResponseBuilder.ok(content)
			.header("Content-Disposition", "attachment; filename=" + filename).build().toResponse();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CRYPTO, op = Operation.WRITE, msg = "Import certificate",
		log = AuditLogType.DATA_OUT)
	public CertificateEntity importCertificate(CertificateEntity certificateEntity,
		FileUpload publicKey, FileUpload privateKey, FileUpload certificate) {
		return certificateService.importCertificate(certificateEntity, publicKey, privateKey,
			certificate);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CRYPTO, op = Operation.DELETE, msg = "Delete certificate")
	public void delete(String id) {
		certificateService.deleteById(id);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CRYPTO, op = Operation.WRITE, msg = "Save certificate")
	public CertificateEntity save(CertificateEntity certificateEntity) {
		return certificateService.save(certificateEntity);
	}
}
