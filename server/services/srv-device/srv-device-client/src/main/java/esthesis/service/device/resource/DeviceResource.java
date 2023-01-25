package esthesis.service.device.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.dto.GeolocationDTO;
import esthesis.service.device.entity.DeviceEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DeviceResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DeviceResource {

  /**
   * Counts the number of devices having one or more of the given tag names.
   *
   * @param tags         A comma-separated list of tag names to search by.
   * @param partialMatch If true, the search will be performed using partial matching.
   */
  @GET
  @Path("/v1/count/by-tag")
  Long countByTags(@QueryParam("tag") String tags,
      @QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

  /**
   * Counts the number of devices having one or more of the given hardware IDs.
   *
   * @param hardwareIds  A comma-separated list of hardware IDs to search by.
   * @param partialMatch Whether to perform a partial match on the hardware IDs.
   */
  @GET
  @Path("/v1/count/by-hardware-id")
  Long countByHardwareIds(@QueryParam("hardwareIds") String hardwareIds,
      @QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

  /**
   * Finds the devices matching a comma-separated list of hardware IDs.
   *
   * @param hardwareIds  The hardware IDs to search by.
   * @param partialMatch Whether to perform a partial match on the hardware IDs.
   */
  @GET
  @Path("/v1/find/by-hardware-id")
  List<DeviceEntity> findByHardwareIds(@QueryParam("hardwareIds") String hardwareIds,
      @QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

  /**
   * Finds the devices matching the given tag names.
   *
   * @param tag The tag name to search by.
   */
  @GET
  @Path("/v1/find/by-tag-name")
  List<DeviceEntity> findByTagName(@QueryParam("tag") String tag);

  @GET
  @Path("/v1/find/by-tag-id")
  List<DeviceEntity> findByTagId(@QueryParam("tag") String tagId);

  @GET
  @Path("/v1/find")
  Page<DeviceEntity> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/{deviceId}")
  DeviceEntity get(@PathParam("deviceId") String id);

  @DELETE
  @Path("/v1/{deviceId}")
  void delete(@PathParam("deviceId") String id);

  @POST
  @Path("/v1")
  DeviceEntity save(@Valid DeviceEntity object);

  @GET
  @Path("/v1/{deviceId}/geolocation")
  GeolocationDTO getDeviceGeolocation(String deviceId);

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/v1/{deviceId}/download/public-key")
  Response downloadPublicKey(@PathParam("deviceId") String id);

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/v1/{deviceId}/download/private-key")
  Response downloadPrivateKey(@PathParam("deviceId") String id);

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/v1/{deviceId}/download/certificate")
  Response downloadCertificate(@PathParam("deviceId") String id);

}
