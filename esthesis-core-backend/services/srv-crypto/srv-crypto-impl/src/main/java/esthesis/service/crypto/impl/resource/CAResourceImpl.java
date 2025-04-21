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
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.resource.CAResource;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * Implementation of the {@link CAResource} interface.
 */
@Authenticated
public class CAResourceImpl implements CAResource {

	@Inject
	CAService caService;

	@GET
	@Override
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	@JSONReplyFilter(filter = "content,content.id,content.cn,content.issued,content.parentCa,"
		+ "content.parentCaId,content.validity,content.name")
	@Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "Search certificate authorities",
		log = AuditLogType.DATA_IN)
	public Page<CaEntity> find(@BeanParam Pageable pageable) {
		return caService.find(pageable);
	}

	@GET
	@Override
	@Path("/v1/find/by-ids")
	@RolesAllowed(AppConstants.ROLE_USER)
	@JSONReplyFilter(filter = "content,content.id,content.cn,content.issued,content.parentCa,"
		+ "content.parentCaId,content.validity,content.name")
	public List<CaEntity> findByIds(@QueryParam("ids")  String id) {
		return caService.findByIds(id);
	}

	@GET
	@Path("/v1/find/by-cn")
	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@JSONReplyFilter(filter = "id,cn,issued,parentCa,validity,parentCaId,name")
	public CaEntity findByCn(@QueryParam("cn") String cn) {
		return caService.findByCn(cn);
	}

	@GET
	@Override
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	@JSONReplyFilter(filter = "id,cn,issued,validity,name,parentCa,parentCaId")
	@Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "View certificate authority")
	public CaEntity findById(String id) {
		return caService.findById(id);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public CaEntity findByIdComplete(String id) {
		return findById(id);
	}

	@GET
	@Override
	@Path("/v1/eligible-for-signing")
	@RolesAllowed(AppConstants.ROLE_USER)
	@JSONReplyFilter(filter = "id,cn,name")
	public List<CaEntity> getEligbleForSigning() {
		return caService.getEligibleForSigning();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "Download certificate authority", log =
		AuditLogType.DATA_IN)
	public Response download(String caId, AppConstants.KeyType type) {
		CaEntity caEntity = caService.findById(caId);

		String content;
		String filename = Slugify.builder().underscoreSeparator(true).build()
			.slugify(caEntity.getCn());
		switch (type) {
			case PRIVATE -> {
				filename += ".key";
				content = caService.getPrivateKey(caId);
			}
			case PUBLIC -> {
				filename += ".pub";
				content = caService.getPublicKey(caId);
			}
			case CERTIFICATE -> {
				filename += ".crt";
				content = String.join("", caService.getCertificate(caId));
			}
			default -> throw new QDoesNotExistException("Key type '{}' is not valid.", type);
		}
		return ResponseBuilder.ok(content)
			.header("Content-Disposition", "attachment; filename=" + filename).build().toResponse();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CRYPTO, op = Operation.WRITE, msg = "Import certificate authority",
		log = AuditLogType.DATA_OUT)
	public CaEntity importCa(CaEntity caEntity, FileUpload publicKey, FileUpload privateKey,
		FileUpload certificate) {
		return caService.importCa(caEntity, publicKey, privateKey, certificate);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CRYPTO, op = Operation.DELETE, msg = "Delete certificate authority")
	public void delete(String id) {
		caService.deleteById(id);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.CRYPTO, op = Operation.WRITE, msg = "Save certificate authority")
	public CaEntity save(CaEntity caEntity) {
		return caService.save(caEntity);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public String getCACertificate(String caId) {
		return findById(caId).getCertificate();
	}
}
