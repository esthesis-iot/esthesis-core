package esthesis.services.campaign.impl.repository;

import esthesis.service.campaign.entity.CampaignEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for @{@link CampaignEntity}.
 */
@ApplicationScoped
public class CampaignRepository implements PanacheMongoRepository<CampaignEntity> {

}
