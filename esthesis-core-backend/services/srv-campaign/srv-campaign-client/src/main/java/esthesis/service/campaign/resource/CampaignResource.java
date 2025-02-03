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

/**
 * REST client for the campaign service.
 */
@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "CampaignResource")
public interface CampaignResource {

	/**
	 * Find all campaigns.
	 *
	 * @param pageable Parameters for pagination.
	 * @return A list of campaigns, paginated.
	 */
	@GET
	@Path("/v1/find")
	Page<CampaignEntity> find(@BeanParam Pageable pageable);

	/**
	 * Save a campaign.
	 *
	 * @param campaignEntity The campaign to save.
	 */
	@POST
	@Path("/v1")
	void save(@Valid CampaignEntity campaignEntity);

	/**
	 * Find a campaign by its ID.
	 *
	 * @param campaignId The ID of the campaign to find.
	 * @return The campaign with the given ID.
	 */
	@GET
	@Path("/v1/{id}")
	CampaignEntity findById(@PathParam("id") String campaignId);

	/**
	 * Resume a previously paused campaign.
	 *
	 * @param campaignId The ID of the campaign to resume.
	 */
	@GET
	@Path("/v1/{id}/resume")
	void resume(@PathParam("id") String campaignId);

	/**
	 * Terminate a running campaign.
	 *
	 * @param campaignId The ID of the campaign to terminate.
	 */
	@GET
	@Path("/v1/{id}/terminate")
	void terminate(@PathParam("id") String campaignId);

	/**
	 * Replay a campaign.
	 *
	 * @param campaignId The ID of the campaign to replay.
	 * @return The replayed campaign.
	 */
	@GET
	@Path("/v1/{id}/replay")
	CampaignEntity replay(@PathParam("id") String campaignId);

	/**
	 * Start a campaign.
	 *
	 * @param campaignId The ID of the campaign to start.
	 */
	@GET
	@Path("/v1/{id}/start")
	void start(@PathParam("id") String campaignId);

	/**
	 * Get statistics for a campaign.
	 *
	 * @param campaignId The ID of the campaign to get statistics for.
	 * @return The statistics for the campaign.
	 */
	@GET
	@Path("/v1/{id}/stats")
	CampaignStatsDTO getCampaignStats(@PathParam("id") String campaignId);

	/**
	 * Delete a campaign.
	 *
	 * @param campaignId The ID of the campaign to delete.
	 */
	@DELETE
	@Path("/v1/{id}")
	Response delete(@PathParam("id") String campaignId);

}

