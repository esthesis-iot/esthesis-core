package esthesis.service.crypto.impl.repository;

import esthesis.service.crypto.entity.CertificateEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CertificateEntityRepository implements
	PanacheMongoRepository<CertificateEntity> {

}
