package esthesis.services.campaign.impl.repository;

import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for @{@link CampaignDeviceMonitorEntity}.
 */
@ApplicationScoped
public class CampaignDeviceMonitorRepository implements
	PanacheMongoRepository<CampaignDeviceMonitorEntity> {

}
