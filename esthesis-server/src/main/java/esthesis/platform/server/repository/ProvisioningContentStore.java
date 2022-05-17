package esthesis.platform.server.repository;

import esthesis.platform.server.model.Provisioning;
import org.springframework.content.commons.repository.ContentStore;
import org.springframework.stereotype.Component;

@Component
public interface ProvisioningContentStore extends ContentStore<Provisioning, String> {

}
