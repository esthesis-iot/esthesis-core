package esthesis.services.dashboard.impl.repository;

import esthesis.service.dashboard.entity.DashboardEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DashboardRepository implements PanacheMongoRepository<DashboardEntity> {

}

