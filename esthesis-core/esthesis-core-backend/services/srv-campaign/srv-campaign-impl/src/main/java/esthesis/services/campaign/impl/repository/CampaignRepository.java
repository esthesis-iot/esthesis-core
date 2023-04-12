package esthesis.services.campaign.impl.repository;

import esthesis.service.campaign.entity.CampaignEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CampaignRepository implements PanacheMongoRepository<CampaignEntity> {

}
