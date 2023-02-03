package esthesis.service.campaign.resource;

import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "CampaignResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CampaignResource {

  @GET
  @Path("/v1/find")
  Page<CampaignEntity> find(@BeanParam Pageable pageable);

  @POST
  @Path("/v1")
  void save(@Valid CampaignEntity campaignEntity);

  @GET
  @Path("/v1/{id}")
  CampaignEntity findById(@PathParam("id") String campaignId);

  @GET
  @Path("/v1/{id}/resume")
  void resume(@PathParam("id") String campaignId);

  @GET
  @Path("/v1/{id}/terminate")
  void terminate(@PathParam("id") String campaignId);

  @GET
  @Path("/v1/{id}/start")
  void start(@PathParam("id") String campaignId);

  @GET
  @Path("/v1/{id}/stats")
  CampaignStatsDTO getCampaignStats(@PathParam("id") String campaignId);

  @DELETE
  @Path("/v1/{id}")
  Response delete(@PathParam("id") String campaignId);

}
