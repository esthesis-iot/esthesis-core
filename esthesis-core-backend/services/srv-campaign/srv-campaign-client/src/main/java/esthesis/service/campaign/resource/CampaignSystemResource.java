package esthesis.service.campaign.resource;

import esthesis.core.common.AppConstants.Campaign.Condition.Stage;
import esthesis.core.common.AppConstants.Campaign.Condition.Type;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * REST client for the campaign system service, when accessed by a SYSTEM OIDC client.
 */
@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "CampaignSystemResource")
public interface CampaignSystemResource {

	/**
	 * Get general statistics about all campaigns.
	 *
	 * @param lastCampaigns The number of campaigns to include in the statistics, starting from the
	 *                      most recent one.
	 * @return A list of statistics.
	 */
	@GET
	@Path("/v1/system/general")
	List<CampaignStatsDTO> getStats(@QueryParam("lastCampaigns") int lastCampaigns);

	/**
	 * Find a campaign by its ID.
	 *
	 * @param campaignId The ID of the campaign to find.
	 * @return The campaign with the given ID.
	 */
	@GET
	@Path("/v1/system/campaign/{id}")
	CampaignEntity findById(@PathParam("id") String campaignId);


	/**
	 * Set the state description of a campaign.
	 *
	 * @param campaignId       The id of the campaign to set the state description for.
	 * @param stateDescription The state description to set.
	 * @return The campaign with the updated state description.
	 */
	@PUT
	@Path("/v1/system/campaign/{id}/state-description")
	CampaignEntity setStateDescription(@PathParam("id") String campaignId, String stateDescription);


	/**
	 * Get the condition for a specific campaign, group, and stage.
	 *
	 * @param campaignId The ID of the campaign.
	 * @param group      The group number.
	 * @param stage      The stage of the campaign.
	 * @param type       The type of  the condition.
	 * @return A list of campaign conditions matching the specified parameters.
	 */
	@GET
	@Path("/v1/system/campaign/{campaign}/group/{group}/stage/{stage}/type/{type}/condition")
	List<CampaignConditionDTO> getCondition(
		@PathParam("campaign") String campaignId,
		@PathParam("group") int group,
		@PathParam("stage") Stage stage,
		@PathParam("type") Type type
	);

	/**
	 * Save or update a campaign.
	 *
	 * @param campaignEntity The campaign to save.
	 */
	@POST
	@Path("/v1/system/campaign")
	void save(@Valid CampaignEntity campaignEntity);

	/**
	 * Find all groups defined for a specific campaign.
	 *
	 * @param campaignId The ID of the campaign.
	 * @return A list of group numbers associated with the specified campaign.
	 */
	@GET
	@Path("/v1/system/campaign/{id}/groups")
	List<Integer> findGroups(@PathParam("id") String campaignId);
}
