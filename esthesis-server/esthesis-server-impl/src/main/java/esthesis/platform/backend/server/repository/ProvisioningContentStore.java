package esthesis.platform.backend.server.repository;

import esthesis.platform.backend.server.model.Provisioning;
import org.springframework.content.commons.repository.ContentStore;
import org.springframework.stereotype.Component;

@Component
public interface ProvisioningContentStore extends ContentStore<Provisioning, String> {

}
