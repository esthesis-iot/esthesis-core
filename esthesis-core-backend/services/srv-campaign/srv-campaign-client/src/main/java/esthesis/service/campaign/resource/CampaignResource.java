package esthesis.service.campaign.resource;

import esthesis.common.AppConstants;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "CampaignResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CampaignResource {

	@GET
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<CampaignEntity> find(@BeanParam Pageable pageable);

	@POST
	@Path("/v1")
	@RolesAllowed(AppConstants.ROLE_USER)
	void save(@Valid CampaignEntity campaignEntity);

	@GET
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	CampaignEntity findById(@PathParam("id") String campaignId);

	@GET
	@Path("/v1/{id}/resume")
	@RolesAllowed(AppConstants.ROLE_USER)
	void resume(@PathParam("id") String campaignId);

	@GET
	@Path("/v1/{id}/terminate")
	@RolesAllowed(AppConstants.ROLE_USER)
	void terminate(@PathParam("id") String campaignId);

	@GET
	@Path("/v1/{id}/replicate")
	@RolesAllowed(AppConstants.ROLE_USER)
	CampaignEntity replicate(@PathParam("id") String campaignId);

	@GET
	@Path("/v1/{id}/start")
	@RolesAllowed(AppConstants.ROLE_USER)
	void start(@PathParam("id") String campaignId);

	@GET
	@Path("/v1/{id}/stats")
	@RolesAllowed(AppConstants.ROLE_USER)
	CampaignStatsDTO getCampaignStats(@PathParam("id") String campaignId);

	@DELETE
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	Response delete(@PathParam("id") String campaignId);

}

