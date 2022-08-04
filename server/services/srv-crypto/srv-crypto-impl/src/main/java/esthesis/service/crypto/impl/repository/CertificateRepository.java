package esthesis.service.crypto.impl.repository;

import esthesis.service.crypto.dto.Certificate;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CertificateRepository implements
    PanacheMongoRepository<Certificate> {

}
