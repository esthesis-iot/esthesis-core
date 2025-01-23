package esthesis.service.crypto.impl.repository;

import esthesis.service.crypto.entity.CertificateEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for the {@link CertificateEntity} entity.
 */
@ApplicationScoped
public class CertificateEntityRepository implements
	PanacheMongoRepository<CertificateEntity> {

}
