package esthesis.services.campaign.impl.repository;

import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CampaignDeviceMonitorRepository implements
    PanacheMongoRepository<CampaignDeviceMonitorEntity> {

}
