package esthesis.service.campaign.resource;

import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.oidc.token.propagation.AccessToken;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "CampaignResource")
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
	@Path("/v1/{id}/replicate")
	CampaignEntity replicate(@PathParam("id") String campaignId);

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

