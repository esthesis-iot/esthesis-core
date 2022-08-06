package esthesis.service.crypto.resource;

import esthesis.common.rest.Page;
import esthesis.common.rest.Pageable;
import esthesis.service.crypto.dto.Store;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/store")
@RegisterRestClient(configKey = "StoreResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface StoreResourceV1 {

  @GET
  @Path("/find")
  Page<Store> find(@BeanParam Pageable pageable);

  @GET
  @Path("/{id}")
  Store findById(@PathParam("id") ObjectId id);

  @POST
  Store save(@Valid Store store);

  @DELETE
  @Path("/{id}")
  void delete(@PathParam("id") ObjectId id);

  @GET
  @Path("/{id}/download")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response download(@PathParam("id") ObjectId id);

//  @GetMapping(value = {"{id}/download"})
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
//      logMessage = "Could not download keystore.")
//  public ResponseEntity download(@PathVariable long id)
//  throws IOException, CertificateException, NoSuchAlgorithmException,
//         KeyStoreException,
//         NoSuchProviderException, InvalidKeySpecException,
//         NoSuchPaddingException,
//         InvalidAlgorithmParameterException, InvalidKeyException {
//    DownloadReply keyDownloadReply = storeService.download(id);
//    return ResponseEntity
//        .ok()
//        .header(HttpHeaders.CONTENT_DISPOSITION,
//            "attachment; filename=" + keyDownloadReply.getFilename())
//        .contentType(MediaType.APPLICATION_OCTET_STREAM)
//        .body(keyDownloadReply.getBinaryPayload());
//  }

}
