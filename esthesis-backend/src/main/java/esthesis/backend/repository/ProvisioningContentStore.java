package esthesis.backend.repository;

import esthesis.backend.model.Provisioning;
import org.springframework.content.commons.repository.ContentStore;
import org.springframework.stereotype.Component;

@Component
public interface ProvisioningContentStore extends ContentStore<Provisioning, String> {

}
