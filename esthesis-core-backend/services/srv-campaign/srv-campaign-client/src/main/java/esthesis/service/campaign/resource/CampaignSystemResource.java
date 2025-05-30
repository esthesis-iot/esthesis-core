package esthesis.service.campaign.resource;

import esthesis.service.campaign.dto.CampaignStatsDTO;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

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
}
